package co.infinum.retromock

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

class KotlinSuspendTest {

  interface Service {
    @GET("/")
    @MockResponse(body = "smith.json")
    @Mock
    suspend fun body(): String

    @GET("/")
    @MockResponse(body = "smith.json")
    @Mock
    suspend fun bodyNullable(): String?

    @GET("/")
    @MockResponse(body = "smith.json")
    @Mock
    suspend fun response(): Response<String>

    @GET("/{a}/{b}/{c}")
    @MockResponse(body = "smith.json")
    @Mock
    suspend fun params(
            @Path("a") a: String,
            @Path("b") b: String,
            @Path("c") c: String
    ): String
  }

  companion object {

    lateinit var service: Service

    @BeforeAll
    fun setup() {
      val retrofit = Retrofit.Builder()
              .baseUrl("https://www.google.com")
              .build()
      val retromock: Retromock = Retromock.Builder()
              .retrofit(retrofit)
              .defaultBehavior(Behavior { 0 })
              .build()
      service = retromock.create(Service::class.java)
    }
  }

  @Test
  fun body() {
    val body = runBlocking { service.body() }
    Assertions.assertThat(body).isEqualTo("Hi")
  }
}

