package co.infinum.retromock

import co.infinum.retromock.helpers.captor
import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@RunWith(MockitoJUnitRunner::class)
class RetromockCallTest {

    @Mock
    lateinit var behavior: Behavior

    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var callbackExecutor: Executor

    private lateinit var retromockCall: RetromockCall<String>

    @Before
    fun setup() {
        backgroundExecutor = MoreExecutors.newDirectExecutorService()
        callbackExecutor = MoreExecutors.directExecutor()
    }

    @Test
    fun http200Sync() {
        val body = "Response body content"
        val delegate = Calls.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()

        assertThat(actualResponse.isSuccessful).isTrue()
        assertThat(actualResponse.body()).isEqualTo(body)
    }

    @Test
    fun http200Async() {
        val body = "Response body content"
        val delegate = Calls.response(body)

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
        val delegate = Calls.response(Response.error<String>(404, ResponseBody.create(MediaType.parse("text/plain"), body)))

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()

        assertThat(actualResponse.code()).isEqualTo(404)
        assertThat(actualResponse.errorBody()?.string()).isEqualTo(body)
    }

    @Test
    fun http404Async() {
        val body = "Response body content"
        val delegate = Calls.response(Response.error<String>(404, ResponseBody.create(MediaType.parse("text/plain"), body)))

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

    @Test(expected = IOException::class)
    fun transportProblem() {
        val delegate = Calls.failure<String>(IOException("Socket closed."))

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        retromockCall.execute()
    }

    @Test
    fun transportProblemAsync() {
        val delegate = Calls.failure<String>(IOException("Socket closed."))

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
        val delegate = Calls.response(body)

        retromockCall = RetromockCall(behavior, backgroundExecutor, callbackExecutor, delegate)
        whenever(behavior.delayMillis()).thenReturn(0)

        val actualResponse = retromockCall.execute()
    }

}