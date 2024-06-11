package co.infinum.retromock

import co.infinum.retromock.helpers.ImmediateBehavior
import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponseProvider
import co.infinum.retromock.meta.ProvidesMock
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.http.GET

class ProviderResponseTests {
    private val retromock: Retromock = Retromock.Builder()
        .retrofit(
            Retrofit.Builder()
                .baseUrl("http://infinum.co")
                .build()
        )
        .build()


    private val service = retromock.create(Service::class.java)

    interface Service {

        @GET("/")
        @Mock
        @MockResponseProvider(NoArgsProducer::class)
        fun noArgs(): ResponseBody

        @GET("/")
        @Mock
        @MockResponseProvider(SingleArgProducer::class)
        fun singleArg(arg: String): ResponseBody

        @GET("/")
        @Mock
        @MockResponseProvider(MultipleArgsProducer::class)
        fun multipleArgs(arg0: String, arg1: String): ResponseBody

        @GET("/")
        @Mock
        @MockResponseProvider(MultipleDiffProducer::class)
        fun multipleDiff(arg0: String, arg1: Int): ResponseBody
    }

    class NoArgsProducer {
        @ProvidesMock
        fun noArgs(): Response =
            Response.Builder().body("noArgsBody").build()

    }

    class SingleArgProducer {
        @ProvidesMock
        fun singleArg(arg: String): Response =
            Response.Builder().body(arg).build()

    }

    class MultipleArgsProducer {
        @ProvidesMock
        fun multipleArgs(arg1: String, arg2: String): Response =
            Response.Builder().body(arg1 + arg2).build()
    }

    class MultipleDiffProducer {
        @ProvidesMock
        fun multipleDiff(arg1: String, arg2: Int): Response =
            Response.Builder().body(arg1 + arg2).build()
    }

    @Test
    fun testNoArgProvider() {
        val responseBody =   service.noArgs()
        Assertions.assertThat(responseBody.string()).isEqualTo("noArgsBody")
    }

    @Test
    fun testSingleArgProvider() {
        val arg1 = "arg1"
        val responseBody =   service.singleArg(arg1)
        Assertions.assertThat(responseBody.string()).isEqualTo(arg1)
    }

    @Test
    fun testMultipleArgsProvider() {
        val arg1 = "arg1"
        val arg2 = "arg2"
        val responseBody =   service.multipleArgs(arg1, arg2)
        Assertions.assertThat(responseBody.string()).isEqualTo(arg1 + arg2)
    }

    @Test
    fun testMultipleDiffProvider() {
        val arg1 = "arg1"
        val arg2 = 10
        val responseBody =   service.multipleDiff(arg1, arg2)
        Assertions.assertThat(responseBody.string()).isEqualTo(arg1 + arg2)
    }
}