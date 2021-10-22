package co.infinum.retromock

import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Callable

@ExtendWith(MockitoExtension::class)
class CallFactoryTest {

    @Test
    fun fakeCallCannotBeCreatedWithBothResponseAndError() {
        assertThrows<AssertionError> {
            CallFactory.FakeCall(Response.success(""), IOException())
        }
    }

    @Test
    fun fakeCallCannotBeCreatedWithNonOfResponseAndError() {
        assertThrows<AssertionError> {
            CallFactory.FakeCall<String>(null, null)
        }
    }

    @Test
    fun fakeCallExecuteSetsTheFlag() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.execute()
        assertThat(call.isExecuted).isTrue()
    }

    @Test
    fun fakeCallCancelSetsTheFlag() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.cancel()
        assertThat(call.isCanceled).isTrue()
    }

    @Test
    fun fakeCallCannotExecuteCallMoreThanOnce() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.execute()

        assertThrows<IllegalStateException> {
            call.execute()
        }
    }

    @Test
    fun fakeCallCannotExecuteCanceledCall() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.cancel()

        assertThrows<IOException> {
            call.execute()
        }
    }

    @Test
    fun fakeCallCloneCreatesCleanState() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.execute()
        call.cancel()

        val clone = call.clone()
        assertThat(clone.isExecuted).isFalse()
        assertThat(clone.isCanceled).isFalse()
    }

    @Test
    fun fakeCallExecuteThrows() {
        val call = CallFactory.FakeCall<String>(null, IOException())

        assertThrows<IOException> {
            call.execute()
        }
    }

    @Test
    fun fakeCallCannotEnqueueCallMoreThanOnce() {
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.execute()

        assertThrows<IllegalStateException> {
            call.enqueue(mock())
        }
    }

    @Test
    fun fakeCallCannotEnqueueCanceledCall() {
        val callback = mock<Callback<String>>()
        val call = CallFactory.FakeCall(Response.success(""), null)
        call.cancel()
        call.enqueue(callback)

        Mockito.verify(callback).onFailure(ArgumentMatchers.eq(call), any(IOException::class.java))
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun fakeCallRequestReturnsRaw() {
        val response = Response.success("")
        val call = CallFactory.FakeCall(response, null)
        assertThat(call.request()).isEqualTo(response.raw().request())
    }

    @Test
    fun fakeCallErrorResponseReturnsNotNull() {
        val call = CallFactory.FakeCall<String>(null, IOException())
        assertThat(call.request()).isNotNull()
    }

    @Test
    fun deferredCallExecuteInvokesCallable() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)

        call.execute()
        verify(callable).call()
        verifyNoMoreInteractions(callable)
    }

    @Test
    fun deferredCallEnqueueInvokesCallable() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)

        call.enqueue(mock())
        verify(callable).call()
        verifyNoMoreInteractions(callable)
    }

    @Test
    fun deferredCallCallableThrowsIO() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenThrow(IOException::class.java)
        val call = CallFactory.DeferredCall(callable)

        val callback = mock<Callback<String>>()
        call.enqueue(callback)

        verify(callback).onFailure(any(), any(IOException::class.java))
    }

    @Test
    fun deferredCallCallableThrowsFatal() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenThrow(Exception::class.java)
        val call = CallFactory.DeferredCall(callable)

        val callback = mock<Callback<String>>()

        assertThrows<IllegalStateException> {
            call.enqueue(callback)
        }

        verifyZeroInteractions(callback)
    }

    @Test
    fun deferredCallExecuteSetsTheFlag() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)
        call.execute()
        assertThat(call.isExecuted).isTrue()
    }

    @Test
    fun deferredCallCancelSetsTheFlag() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)
        call.cancel()
        assertThat(call.isCanceled).isTrue()
    }

    @Test
    fun deferredCallCannotExecuteCallMoreThanOnce() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)
        call.execute()

        assertThrows<IllegalStateException> {
            call.execute()
        }
    }

    @Test
    fun deferredCallCannotExecuteCanceledCall() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)
        call.cancel()

        assertThrows<IOException> {
            call.execute()
        }
    }

    @Test
    fun deferredCallCloneCreatesCleanState() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).then { CallFactory.response("") }
        val call = CallFactory.DeferredCall(callable)
        call.execute()
        call.cancel()

        val clone = call.clone()
        assertThat(clone.isExecuted).isFalse()
        assertThat(clone.isCanceled).isFalse()
    }

    @Test
    fun deferredCallRequestReturnsRaw() {
        val response = Response.success("")
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(response))
        val call = CallFactory.DeferredCall(callable)
        assertThat(call.request()).isEqualTo(response.raw().request())
    }

    @Test
    fun deferredCallErrorResponseReturnsNotNull() {
        val callable = mock<Callable<Call<String>>>()
        whenever(callable.call()).thenReturn(CallFactory.response(""))
        val call = CallFactory.DeferredCall(callable)
        assertThat(call.request()).isNotNull()
    }

}