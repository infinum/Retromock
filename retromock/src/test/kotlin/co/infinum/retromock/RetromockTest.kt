package co.infinum.retromock

import co.infinum.retromock.helpers.mock
import okhttp3.ResponseBody
import org.assertj.core.api.Java6Assertions.assertThat
import org.assertj.core.api.Java6Assertions.entry
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.InputStream
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

class RetromockTest {

    interface CallMethod {

        @GET("/")
        fun getResponseBody(): Call<ResponseBody>

    }

    class EmptyBodyFactory : BodyFactory {
        override fun create(input: String): InputStream {
            throw NotImplementedError("Method shouldn't be used.")
        }
    }

    class EmptyBodyFactory2 : BodyFactory {
        override fun create(input: String): InputStream {
            throw NotImplementedError("Method shouldn't be used.")
        }
    }

    @Test
    fun objectMethodsStillWork() {
        val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                .baseUrl("http://infinum.co/")
                .build())
            .build()

        val obj = retromock.create(CallMethod::class.java)

        assertThat(obj.hashCode()).isNotZero()
        assertThat(obj == Any()).isFalse()
        assertThat(obj.toString()).isNotEmpty()
    }

    @Test
    fun cloneSharesStatefulInstances() {
        val bodyFactory = EmptyBodyFactory()
        val defaultBodyFactory = EmptyBodyFactory2()
        val defaultBehavior = mock<Behavior>()
        val backgroundExecutor = mock<ExecutorService>()
        val callbackExecutor = mock<Executor>()

        val first = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                .baseUrl("http://infinum.co/")
                .build())
            .addBodyFactory(bodyFactory)
            .defaultBodyFactory(defaultBodyFactory)
            .defaultBehavior(defaultBehavior)
            .backgroundExecutor(backgroundExecutor)
            .callbackExecutor(callbackExecutor)
            .build()

        val bodyFactory2 = EmptyBodyFactory2()

        val second = first.newBuilder()
            .addBodyFactory(bodyFactory2)
            .build()

        assertThat(first.bodyFactories().size).isEqualTo(second.bodyFactories().size - 1)
        assertThat(first.bodyFactories()).contains(
            entry(EmptyBodyFactory::class.java, bodyFactory)
        )

        assertThat(second.bodyFactories()).contains(
            entry(EmptyBodyFactory::class.java, bodyFactory),
            entry(EmptyBodyFactory2::class.java, bodyFactory2)
        )

        assertThat(first.defaultBodyFactory()).isSameAs(second.defaultBodyFactory())
        assertThat(first.defaultBehavior()).isSameAs(second.defaultBehavior())
        assertThat(first.callbackExecutor()).isSameAs(second.callbackExecutor())
        assertThat(first.backgroundExecutor()).isSameAs(second.backgroundExecutor())

    }

}