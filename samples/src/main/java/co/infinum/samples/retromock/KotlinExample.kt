package co.infinum.samples.retromock

import co.infinum.retromock.Behavior
import co.infinum.retromock.Retromock
import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import com.squareup.moshi.Json
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

class KotlinExample {

    interface Service {
        @GET("/")
        @MockResponse(body = "smith.json")
        @Mock
        suspend fun getCoroutineUser(): User?

        @GET("/")
        @MockResponse(body = "smith.json")
        @Mock
        suspend fun getCoroutineUserWithResponse(): Response<User>
    }

    class User {
        @Json(name = "name")
        var name: String? = null
        @Json(name = "surname")
        var surname: String? = null

        override fun toString(): String {
            return ("User{"
                    + "name='" + name + '\''
                    + ", surname='" + surname + '\''
                    + '}')
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.google.com")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            val retromock: Retromock = Retromock.Builder()
                    .retrofit(retrofit)
                    .defaultBehavior(Behavior { 0 })
                    .defaultBodyFactory(ResourceBodyFactory())
                    .build()
            val service: Service = retromock.create(Service::class.java)

            runBlocking {
                println("Inside run blocking")

                val user = withContext(Dispatchers.IO) {
                    println("Calling coroutine")
                    service.getCoroutineUserWithResponse().body()
                }

                withContext(Dispatchers.Default) {
                    println(user)
                }
            }
        }
    }
}