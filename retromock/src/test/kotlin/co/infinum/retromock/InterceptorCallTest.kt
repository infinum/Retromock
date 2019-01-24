package co.infinum.retromock

import co.infinum.retromock.helpers.captor
import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@ExtendWith(MockitoExtension::class)
class InterceptorCallTest {

    @Mock
    private lateinit var behavior: Behavior

    @Mock
    private lateinit var producer: ParamsProducer

    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var callbackExecutor: Executor

    private val request = Request.Builder().url("http://localhost").build()
    private lateinit var retromock: Retromock

    @BeforeEach
    fun setup() {
        backgroundExecutor = MoreExecutors.newDirectExecutorService()
        callbackExecutor = MoreExecutors.directExecutor()

        retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                .baseUrl("http://infinum.co")
                .build())
            .backgroundExecutor(backgroundExecutor)
            .callbackExecutor(callbackExecutor)
            .build()
    }

    @Test
    fun http200Sync() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        val actualResponse = call.execute()

        Assertions.assertThat(actualResponse.isSuccessful).isTrue()
        Assertions.assertThat(actualResponse.body()?.string()).isEqualTo(body)
    }

    @Test
    fun http200Async() {
        val body = "Response body content"

        val callback = mock<Callback>()

        val callCaptor = captor<Call>()
        val responseCaptor = captor<Response>()

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        call.enqueue(callback)

        Mockito.verify(callback).onResponse(callCaptor.capture(), responseCaptor.capture())

        Assertions.assertThat(callCaptor.value.isExecuted).isTrue()
        Assertions.assertThat(responseCaptor.value.isSuccessful).isTrue()
        Assertions.assertThat(responseCaptor.value.body()?.string()).isEqualTo(body)

        Mockito.verifyNoMoreInteractions(callback)
    }

    @Test
    fun http404Sync() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(404)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        val actualResponse = call.execute()

        Assertions.assertThat(actualResponse.code()).isEqualTo(404)
        Assertions.assertThat(actualResponse.body()?.string()).isEqualTo(body)
    }

    @Test
    fun http404Async() {
        val body = "Response body content"

        val callback = mock<Callback>()

        val callCaptor = captor<Call>()
        val responseCaptor = captor<Response>()

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(404)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        call.enqueue(callback)

        Mockito.verify(callback).onResponse(callCaptor.capture(), responseCaptor.capture())

        Assertions.assertThat(callCaptor.value.isExecuted).isTrue()
        Assertions.assertThat(responseCaptor.value.code()).isEqualTo(404)
        Assertions.assertThat(responseCaptor.value.body()?.string()).isEqualTo(body)

        Mockito.verifyNoMoreInteractions(callback)
    }

    @Test
    fun transportProblem() {
        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        val bodyFactory = mock<BodyFactory>()

        val params = ResponseParams.Builder()
            .bodyFactory(RetromockBodyFactory(bodyFactory, ""))
            .build()

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(params)
        whenever(bodyFactory.create(anyString())).thenThrow(IOException("Socket closed."))

        assertThrows<IOException> {
            call.execute()
        }
    }

    @Test
    fun transportProblemAsync() {
        val callback = mock<Callback>()

        val callCaptor = captor<Call>()
        val errorCaptor = captor<IOException>()

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        val bodyFactory = mock<BodyFactory>()

        val params = ResponseParams.Builder()
            .bodyFactory(RetromockBodyFactory(bodyFactory, ""))
            .build()

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(params)
        whenever(bodyFactory.create(anyString())).thenThrow(IOException("Socket closed."))

        call.enqueue(callback)

        Mockito.verify(callback).onFailure(callCaptor.capture(), errorCaptor.capture())

        Assertions.assertThat(callCaptor.value.isExecuted).isTrue()
        Assertions.assertThat(errorCaptor.value).isInstanceOf(IOException::class.java)
        Assertions.assertThat(errorCaptor.value.message).isEqualTo("Socket closed.")

        Mockito.verifyNoMoreInteractions(callback)
    }

    @Test
    fun executeCallOnce() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        val actualResponse = call.execute()

        Assertions.assertThat(actualResponse.body()?.string()).isEqualTo(body)
    }

    @Test
    fun cannotExecuteTwice() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        call.execute()

        assertThrows<IllegalStateException> {
            call.execute()
        }
    }

    @Test
    fun cannotEnqueueTwice() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        call.execute()

        assertThrows<IllegalStateException> {
            call.enqueue(mock())
        }
    }

    @Test
    fun cannotExecuteCancelledCall() {
        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        call.cancel()

        assertThrows<IOException> {
            call.execute()
        }
    }

    @Test
    fun cannotEnqueueCancelledCall() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        val callback = mock<Callback>()

        call.cancel()
        call.enqueue(callback)

        Mockito.verify(callback).onFailure(Mockito.any(), Mockito.any(IOException::class.java))
        Mockito.verifyNoMoreInteractions(callback)
    }

    @Test
    fun executeFlagIsSet() {
        val body = "Response body content"

        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), body))
            .build()
        )

        call.execute()

        Assertions.assertThat(call.isExecuted).isTrue()
    }

    @Test
    fun cancelFlagIsSet() {
        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        call.cancel()

        Assertions.assertThat(call.isCanceled).isTrue()
    }

    @Test
    fun cloneBuildCleanState() {
        val call = InterceptorCall(
            request,
            retromock,
            producer,
            behavior
        )

        whenever(behavior.delayMillis()).thenReturn(0)
        whenever(producer.produce()).thenReturn(ResponseParams.Builder()
            .code(200)
            .bodyFactory(RetromockBodyFactory(PassThroughBodyFactory(), ""))
            .build()
        )

        call.execute()
        call.cancel()

        val cloned = call.clone()

        Assertions.assertThat(cloned.isCanceled).isFalse()
        Assertions.assertThat(cloned.isExecuted).isFalse()
    }

}
