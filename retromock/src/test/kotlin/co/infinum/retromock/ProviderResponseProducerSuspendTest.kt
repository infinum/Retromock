package co.infinum.retromock

import co.infinum.retromock.meta.ProvidesMock
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("UNUSED_PARAMETER", "unused")
@ExtendWith(MockitoExtension::class)
class ProviderResponseProducerSuspendTest {

    val retromock: Retromock = Retromock.Builder()
        .retrofit(
            Retrofit.Builder()
                .baseUrl("http://infinum.co")
                .build()
        )
        .build()

    interface Service {

        suspend fun noArgs(): String

        suspend fun singleArg(arg: String): String

        suspend fun multipleArgs(arg0: String, arg1: String): String

        suspend fun multipleDiff(arg0: String, arg1: Int): String

        suspend fun annotatedArgs(
            @Header("header") header: String,
            @Body body: String
        ): String

        @GET("test")
        suspend fun annotatedMethod(arg0: String, arg1: Int): String

    }

    @Test
    fun findNoArgumentsMethod() {
        class NoArgsProducer {
            @ProvidesMock
            suspend fun noArgs(): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            NoArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod("noArgs"),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(NoArgsProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(NoArgsProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            NoArgsProducer::class.java.getDeclaredSuspendMethod(
                "noArgs"
            )
        )
    }

    @Test
    fun findSingleArgumentsMethod() {
        class SingleArgProducer {
            @ProvidesMock
            suspend fun singleArg(arg: String): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            SingleArgProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod("singleArg", String::class.java),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(SingleArgProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(SingleArgProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            SingleArgProducer::class.java.getDeclaredSuspendMethod(
                "singleArg",
                String::class.java
            )
        )
    }

    @Test
    fun findMultipleArgumentsMethod() {
        class MultipleArgsProducer {
            @ProvidesMock
            suspend fun multipleArgs(arg0: String, arg1: String): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            MultipleArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod(
                "multipleArgs",
                String::class.java,
                String::class.java
            ),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(MultipleArgsProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(MultipleArgsProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            MultipleArgsProducer::class.java.getDeclaredSuspendMethod(
                "multipleArgs",
                String::class.java,
                String::class.java
            )
        )
    }

    @Test
    fun findMultipleDiffArgumentsMethod() {
        class MultipleDiffArgsProducer {
            @ProvidesMock
            suspend fun multipleArgs(arg0: String, arg1: Int): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            MultipleDiffArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod(
                "multipleDiff",
                String::class.java,
                Int::class.java
            ),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(MultipleDiffArgsProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(MultipleDiffArgsProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            MultipleDiffArgsProducer::class.java.getDeclaredSuspendMethod(
                "multipleArgs",
                String::class.java,
                Int::class.java
            )
        )
    }

    @Test
    fun findAnnotatedArgumentsMethod() {
        class MultipleArgsProducer {
            @ProvidesMock
            suspend fun multipleArgs(arg0: String, arg1: String): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            MultipleArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod(
                "annotatedArgs",
                String::class.java,
                String::class.java
            ),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(MultipleArgsProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(MultipleArgsProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            MultipleArgsProducer::class.java.getDeclaredSuspendMethod(
                "multipleArgs",
                String::class.java,
                String::class.java
            )
        )
    }

    @Test
    fun findAnnotatedMethod() {
        class MultipleArgsProducer {
            @ProvidesMock
            suspend fun multipleArgs(arg0: String, arg1: Int): Response = Response.Builder().build()
        }

        val producer = ProviderResponseProducer(
            MultipleArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod(
                "annotatedMethod",
                String::class.java,
                Int::class.java
            ),
            retromock
        )

        assertThat(producer.provider()).isNotNull()
        assertThat(producer.provider()).isInstanceOf(MultipleArgsProducer::class.java)

        assertThat(producer.providerMethod().declaringClass).isEqualTo(MultipleArgsProducer::class.java)
        assertThat(producer.providerMethod()).isEqualTo(
            MultipleArgsProducer::class.java.getDeclaredSuspendMethod(
                "multipleArgs",
                String::class.java,
                Int::class.java
            )
        )
    }

    @Test
    fun methodWithoutProvidesMockAnnotationThrows() {
        class NotAnnotatedMethod {
            suspend fun method(): Response = Response.Builder().build()
        }

        val error = assertThrows<IllegalArgumentException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("Couldn't find a single method annotated with @ProvidesMock in provider class")
    }

    @Test
    fun methodWithInvalidReturnTypeThrows() {
        class NotAnnotatedMethod {
            @ProvidesMock
            suspend fun method(): String = ""
        }

        val error = assertThrows<IllegalArgumentException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("Couldn't find a single method annotated with @ProvidesMock in provider class")
    }

    @Test
    fun methodWithInvalidSignatureThrows() {
        class NotAnnotatedMethod {
            @ProvidesMock
            suspend fun method(someArg: String): Response = Response.Builder().build()
        }

        val error = assertThrows<IllegalArgumentException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("Couldn't find a single method annotated with @ProvidesMock in provider class")
    }

    @Test
    fun moreThanOneMethodsWithSameSignatureThrows() {
        class NotAnnotatedMethod {
            @ProvidesMock
            suspend fun method1(): Response = Response.Builder().build()

            @ProvidesMock
            suspend fun method2(): Response = Response.Builder().build()
        }

        val error = assertThrows<IllegalArgumentException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("More than one annotated mock provider methods with same signature found")
    }

    @Test
    fun methodIsNotPublic() {
        class NotAnnotatedMethod {
            @ProvidesMock
            suspend private fun method(): Response = Response.Builder().build()
        }

        val error = assertThrows<RuntimeException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("Method annotated with @ProvidesMock should be public and concrete.")
    }

    @Test
    fun methodIsAbstract() {
        abstract class NotAnnotatedMethod {
            @ProvidesMock
            abstract suspend fun method(): Response
        }

        val error = assertThrows<RuntimeException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).startsWith("Method annotated with @ProvidesMock should be public and concrete.")
    }

    @Test
    fun throwIfAbstractClass() {
        abstract class NotAnnotatedMethod {
            @ProvidesMock
            suspend fun method(): Response = Response.Builder().build()
        }

        val error = assertThrows<RuntimeException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).contains("shouldn't be an abstract class.")
    }

    @Test
    fun throwIfNoDefaultConstructor() {
        class NotAnnotatedMethod(val any: Any) {
            @ProvidesMock
            suspend fun method(): Response = Response.Builder().build()
        }

        val error = assertThrows<RuntimeException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).contains("has no default constructor.")
    }

    @Test
    fun throwIfConstructorThrows() {
        class NotAnnotatedMethod {

            init {
                throw IllegalArgumentException()
            }

            @Suppress("UNREACHABLE_CODE")
            @ProvidesMock
            suspend fun method(): Response = Response.Builder().build()
        }

        val error = assertThrows<RuntimeException> {
            ProviderResponseProducer(
                NotAnnotatedMethod::class.java,
                Service::class.java.getDeclaredSuspendMethod("noArgs"),
                retromock
            )
        }
        assertThat(error.message).contains("threw an exception during initialization.")
    }

    @Test
    fun throwIfFunctionInvokeThrows() {
        class NoArgsProducer {
            @ProvidesMock
            suspend fun noArgs(): Response = throw IllegalArgumentException()
        }

        val producer = ProviderResponseProducer(
            NoArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod("noArgs"),
            retromock
        )

        val error = assertThrows<RuntimeException> {
            runBlocking { producer.invokeNoArgs() }
        }

        assertThat(error.message).contains("threw an exception while executing.")
        assertThat(error.cause).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun responseCorrectlyMapped() {
        class NoArgsProducer {
            @ProvidesMock
            suspend fun noArgs(): Response = Response.Builder()
                .body("test-body")
                .bodyFactory(PassThroughBodyFactory::class.java)
                .code(100)
                .message("test-message")
                .headers(
                    Headers.of(
                        "test-header", "test-value",
                        "test-header2", "test-value2"
                    )
                )
                .build()
        }

        val producer = ProviderResponseProducer(
            NoArgsProducer::class.java,
            Service::class.java.getDeclaredSuspendMethod("noArgs"),
            retromock
        )

        val params = runBlocking { producer.invokeNoArgs() }

        assertThat(params.code()).isEqualTo(100)
        assertThat(params.message()).isEqualTo("test-message")
        assertThat(params.headers()).isEqualTo(
            Headers.of(
                "test-header", "test-value",
                "test-header2", "test-value2"
            )
        )
        assertThat(params.bodyFactory()).isInstanceOf(RetromockBodyFactory::class.java)
    }

    private suspend fun ProviderResponseProducer.invokeNoArgs(): ResponseParams {
        val value = AtomicReference<ResponseParams>()
        suspendCoroutine<String> {
            val params = produce(arrayOf(it))
            value.set(params)
            it.resume("")
        }
        return value.get()
    }
}