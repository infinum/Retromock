package co.infinum.retromock

import co.infinum.retromock.helpers.captor
import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@ExtendWith(MockitoExtension::class)
class RetromockCallTest {

    @Mock
    lateinit var behavior: Behavior

    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var callbackExecutor: Executor

    private lateinit var retromockCall: RetromockCall<String>

    @BeforeEach
    fun setup() {
        backgroundExecutor = MoreExecutors.newDirectExecutorService()
        callbackExecutor = MoreExecutors.directExecutor()
    }

    @Test
    fun http200Sync() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()

        assertThat(actualResponse.isSuccessful).isTrue()
        assertThat(actualResponse.body()).isEqualTo(body)
    }

    @Test
    fun http200Async() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        val callback = mock<Callback<String>>()

        val callCaptor = captor<Call<String>>()
        val responseCaptor = captor<Response<String>>()

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.enqueue(callback)

        verify(callback).onResponse(callCaptor.capture(), responseCaptor.capture())

        assertThat(callCaptor.value.isExecuted).isTrue()
        assertThat(responseCaptor.value.isSuccessful).isTrue()
        assertThat(responseCaptor.value.body()).isEqualTo(body)

        verifyNoMoreInteractions(callback)
    }

    @Test
    fun http404Sync() {
        val body = "Response body content"
        val delegate = CallFactory.response(Response.error<String>(404, ResponseBody.create(MediaType.parse("text/plain"), body)))

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()

        assertThat(actualResponse.code()).isEqualTo(404)
        assertThat(actualResponse.errorBody()?.string()).isEqualTo(body)
    }

    @Test
    fun http404Async() {
        val body = "Response body content"
        val delegate = CallFactory.response(Response.error<String>(404, ResponseBody.create(MediaType.parse("text/plain"), body)))

        val callback = mock<Callback<String>>()

        val callCaptor = captor<Call<String>>()
        val responseCaptor = captor<Response<String>>()

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.enqueue(callback)

        verify(callback).onResponse(callCaptor.capture(), responseCaptor.capture())

        assertThat(callCaptor.value.isExecuted).isTrue()
        assertThat(responseCaptor.value.code()).isEqualTo(404)
        assertThat(responseCaptor.value.errorBody()?.string()).isEqualTo(body)

        verifyNoMoreInteractions(callback)
    }

    @Test
    fun transportProblem() {
        val delegate = CallFactory.failure<String>(IOException("Socket closed."))

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        assertThrows<IOException> {
            retromockCall.execute()
        }
    }

    @Test
    fun transportProblemAsync() {
        val delegate = CallFactory.failure<String>(IOException("Socket closed."))

        val callback = mock<Callback<String>>()

        val callCaptor = captor<Call<String>>()
        val errorCaptor = captor<Throwable>()

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.enqueue(callback)

        verify(callback).onFailure(callCaptor.capture(), errorCaptor.capture())

        assertThat(callCaptor.value.isExecuted).isTrue()
        assertThat(errorCaptor.value).isInstanceOf(IOException::class.java)
        assertThat(errorCaptor.value.message).isEqualTo("Socket closed.")

        verifyNoMoreInteractions(callback)
    }

    @Test
    fun executeCallOnce() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()

        assertThat(actualResponse.body()).isEqualTo(body)
    }

    @Test
    fun requestIsEqualToRaw() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)

        assertThat(retromockCall.request()).isEqualTo(delegate.request())
    }

    @Test
    fun cannotExecuteTwice() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.execute()

        assertThrows<IllegalStateException> {
            retromockCall.execute()
        }
    }

    @Test
    fun cannotEnqueueTwice() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.execute()

        assertThrows<IllegalStateException> {
            retromockCall.enqueue(mock())
        }
    }

    @Test
    fun cannotExecuteCancelledCall() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)

        retromockCall.cancel()

        assertThrows<IOException> {
            retromockCall.execute()
        }
    }

    @Test
    fun cannotEnqueueCancelledCall() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        val callback = mock<Callback<String>>()

        retromockCall.cancel()
        retromockCall.enqueue(callback)

        verify(callback).onFailure(any(), any(IOException::class.java))
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun executeFlagIsSet() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        retromockCall.execute()

        assertThat(retromockCall.isExecuted).isTrue()
    }

    @Test
    fun cancelFlagIsSet() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        retromockCall.cancel()

        assertThat(retromockCall.isCanceled).isTrue()
    }

    @Test
    fun cloneBuildCleanState() {
        val body = "Response body content"
        val delegate = CallFactory.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        retromockCall.execute()
        retromockCall.cancel()

        val cloned = retromockCall.clone()

        assertThat(cloned.isCanceled).isFalse()
        assertThat(cloned.isExecuted).isFalse()
    }

}