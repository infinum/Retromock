package co.infinum.retromock

import co.infinum.retromock.helpers.captor
import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import okhttp3.*
import okhttp3.internal.Util
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Converter
import retrofit2.Response

@ExtendWith(MockitoExtension::class)
class MockedCallTest {

    @Mock
    private lateinit var rawCall: Call

    @Mock
    private lateinit var converter: Converter<ResponseBody, Any>

    @Test
    fun isExecuteDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        call.isExecuted

        verify(rawCall).isExecuted
    }

    @Test
    fun isCancelDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        call.isCanceled

        verify(rawCall).isCanceled
    }

    @Test
    fun cancelDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        call.cancel()

        verify(rawCall).cancel()
    }

    @Test
    fun requestDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        call.request()

        verify(rawCall).request()
    }

    @Test
    fun executeDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        val response = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.execute()).thenReturn(response)

        call.execute()

        verify(rawCall).execute()
    }

    @Test
    fun executeCallsConverter() {
        val call = MockedCall(converter, rawCall)

        val response = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.execute()).thenReturn(response)

        call.execute()

        verify(converter).convert(response.body()!!)
    }

    @Test
    fun executeConvertsToRetrofitCall() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        val result = Any()

        whenever(rawCall.execute()).thenReturn(okhttpResponse)
        whenever(converter.convert(any())).thenReturn(result)

        val retrofitResponse = call.execute()

        assertThat(retrofitResponse.raw()).isSameAs(okhttpResponse)
        assertThat(retrofitResponse.body()).isSameAs(result)
        assertThat(retrofitResponse.code()).isEqualTo(okhttpResponse.code())
        assertThat(retrofitResponse.message()).isEqualTo(okhttpResponse.message())
        assertThat(retrofitResponse.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun executeErrorResponse() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(404)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.execute()).thenReturn(okhttpResponse)

        val retrofitResponse = call.execute()

        assertThat(retrofitResponse.raw()).isSameAs(okhttpResponse)
        assertThat(retrofitResponse.errorBody()).isSameAs(okhttpResponse.body())
        assertThat(retrofitResponse.code()).isEqualTo(okhttpResponse.code())
        assertThat(retrofitResponse.message()).isEqualTo(okhttpResponse.message())
        assertThat(retrofitResponse.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun executeClosesBodyForNoContent() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(204)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.execute()).thenReturn(okhttpResponse)

        val retrofitResponse = call.execute()

        assertThat(retrofitResponse.raw()).isSameAs(okhttpResponse)
        assertThat(retrofitResponse.body()).isSameAs(null)
        assertThat(retrofitResponse.code()).isEqualTo(okhttpResponse.code())
        assertThat(retrofitResponse.message()).isEqualTo(okhttpResponse.message())
        assertThat(retrofitResponse.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun executeClosesBodyForReset() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(205)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.execute()).thenReturn(okhttpResponse)

        val retrofitResponse = call.execute()

        assertThat(retrofitResponse.raw()).isSameAs(okhttpResponse)
        assertThat(retrofitResponse.body()).isSameAs(null)
        assertThat(retrofitResponse.code()).isEqualTo(okhttpResponse.code())
        assertThat(retrofitResponse.message()).isEqualTo(okhttpResponse.message())
        assertThat(retrofitResponse.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun enqueueDelegatesToRaw() {
        val call = MockedCall(converter, rawCall)

        val response = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, response)
        }

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        verify(rawCall).enqueue(any())
    }

    @Test
    fun enqueueCallsConverter() {
        val call = MockedCall(converter, rawCall)

        val response = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, response)
        }

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        verify(converter).convert(response.body()!!)
    }

    @Test
    fun enqueueConvertsToRetrofitCall() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(200)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        val result = Any()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, okhttpResponse)
        }
        whenever(converter.convert(any())).thenReturn(result)

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        val captor = captor<Response<Any>>()

        verify(callback).onResponse(eq(call), captor.capture())

        assertThat(captor.value.raw()).isSameAs(okhttpResponse)
        assertThat(captor.value.body()).isSameAs(result)
        assertThat(captor.value.code()).isEqualTo(okhttpResponse.code())
        assertThat(captor.value.message()).isEqualTo(okhttpResponse.message())
        assertThat(captor.value.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun enqueueErrorResponse() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(404)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, okhttpResponse)
        }

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        val captor = captor<Response<Any>>()

        verify(callback).onResponse(eq(call), captor.capture())

        assertThat(captor.value.raw()).isSameAs(okhttpResponse)
        assertThat(captor.value.errorBody()).isSameAs(okhttpResponse.body())
        assertThat(captor.value.code()).isEqualTo(okhttpResponse.code())
        assertThat(captor.value.message()).isEqualTo(okhttpResponse.message())
        assertThat(captor.value.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun enqueueClosesBodyForNoContent() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(204)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, okhttpResponse)
        }

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        val captor = captor<Response<Any>>()

        verify(callback).onResponse(eq(call), captor.capture())

        assertThat(captor.value.raw()).isSameAs(okhttpResponse)
        assertThat(captor.value.body()).isSameAs(null)
        assertThat(captor.value.code()).isEqualTo(okhttpResponse.code())
        assertThat(captor.value.message()).isEqualTo(okhttpResponse.message())
        assertThat(captor.value.headers()).isEqualTo(okhttpResponse.headers())
    }

    @Test
    fun enqueueClosesBodyForReset() {
        val call = MockedCall(converter, rawCall)

        val okhttpResponse = okhttp3.Response.Builder()
            .code(205)
            .message("OK")
            .body(Util.EMPTY_RESPONSE)
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost").build())
            .build()

        whenever(rawCall.enqueue(any())).thenAnswer {
            (it.arguments.first() as Callback).onResponse(rawCall, okhttpResponse)
        }

        val callback = mock<retrofit2.Callback<Any>>()

        call.enqueue(callback)

        val captor = captor<Response<Any>>()

        verify(callback).onResponse(eq(call), captor.capture())

        assertThat(captor.value.raw()).isSameAs(okhttpResponse)
        assertThat(captor.value.body()).isSameAs(null)
        assertThat(captor.value.code()).isEqualTo(okhttpResponse.code())
        assertThat(captor.value.message()).isEqualTo(okhttpResponse.message())
        assertThat(captor.value.headers()).isEqualTo(okhttpResponse.headers())
    }

}