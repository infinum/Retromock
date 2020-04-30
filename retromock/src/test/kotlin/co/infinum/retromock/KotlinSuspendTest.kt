package co.infinum.retromock

import co.infinum.retromock.helpers.mock
import co.infinum.retromock.helpers.whenever
import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import retrofit2.HttpException
import java.nio.charset.StandardCharsets

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinSuspendTest {

  interface Service {
    @GET("/")
    @MockResponse(body = "Body example.")
    @Mock
    suspend fun body(): ResponseBody

    @GET("/")
    @MockResponse(body = "Body example.")
    @Mock
    suspend fun bodyNullable(): ResponseBody?

    @GET("/")
    @MockResponse(body = "Body example.")
    @Mock
    suspend fun response(): Response<ResponseBody>

    @GET("/{a}/{b}/{c}")
    @MockResponse(body = "Body example.")
    @Mock
    suspend fun params(
            @Path("a") a: String,
            @Path("b") b: String,
            @Path("c") c: String
    ): ResponseBody
  }

  lateinit var service: Service

  @BeforeAll
  fun setup() {
    val factory = mock<BodyFactory>()
    whenever(factory.create(Mockito.anyString())).then {
      (it.arguments[0] as String).byteInputStream(StandardCharsets.UTF_8)
    }

    val retrofit = Retrofit.Builder()
            .baseUrl("https://www.google.com")
            .build()
    val retromock: Retromock = Retromock.Builder()
            .retrofit(retrofit)
            .defaultBodyFactory(factory)
            .defaultBehavior(Behavior { 0 })
            .build()
    service = retromock.create(Service::class.java)
  }

  @Test
  fun body() {
    val body = runBlocking { service.body() }
    Assertions.assertThat(body.string()).isEqualTo("Body example.")
  }

  @Test
  fun body404() {
    try {
      runBlocking { service.body() }
    } catch (e: HttpException) {
      Assertions.assertThat(e.code()).isEqualTo(404)
    }
  }

  @Test
  fun bodyThrowsOnNull() {
    try {
      runBlocking { service.body() }
    } catch (e: KotlinNullPointerException) {
      // Coroutines wraps exceptions with a synthetic trace so fall back to cause message.
      val message = e.message ?: (e.cause as KotlinNullPointerException).message
      Assertions.assertThat(message).isEqualTo(
              "Response from retrofit2.KotlinSuspendTest\$Service.body was null but response body type was declared as non-null")
    }
  }
}

