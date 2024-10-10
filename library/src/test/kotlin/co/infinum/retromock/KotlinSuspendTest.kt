package co.infinum.retromock

import co.infinum.retromock.helpers.*
import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import co.infinum.retromock.meta.MockResponseProvider
import co.infinum.retromock.meta.MockResponses
import co.infinum.retromock.meta.MockSequential
import co.infinum.retromock.meta.ProvidesMock
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Java6Assertions.entry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class KotlinSuspendTest {

    interface CallMethod {

        @GET("/")
        suspend fun getResponseBody(): ResponseBody

    }

    interface ResponseCallMethod {

        @Mock
        @MockResponse(body = "Body example.")
        @GET("/")
        suspend fun getResponseBody(): Response<ResponseBody>

    }

    interface MockedMethod {

        @Mock
        @GET("/")
        suspend fun getResponseBody(): ResponseBody

    }

    interface ResponseMethod {

        @Mock
        @MockResponse(body = "Body example.")
        @GET("/")
        suspend fun getResponseBody(): ResponseBody

    }

    interface ThreeResponsesMethod {

        @Mock
        @MockResponses(
            MockResponse(body = "Body example."),
            MockResponse(body = "Body example 2."),
            MockResponse(body = "Body example 3.")
        )
        @MockSequential
        @GET("/")
        suspend fun getResponseBody(): ResponseBody

    }

    interface ResponseMethodWithCustomBodyFactory {

        @Mock
        @MockResponse(body = "Body example.", bodyFactory = CountDownBodyFactory::class)
        @GET("/")
        suspend fun getResponseBody(): ResponseBody

    }

    interface ThreeResponsesMethodWithCustomBodyFactory {

        @Mock
        @MockResponses(
            MockResponse(body = "Body example."),
            MockResponse(body = "Body example 2.", bodyFactory = PassThroughBodyFactory::class),
            MockResponse(body = "Body example 3.", bodyFactory = CountDownBodyFactory::class)
        )
        @MockSequential
        @GET("/")
        suspend fun getResponseBody(): ResponseBody
    }

    @Test
    fun objectMethodsStillWork() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        val obj = retromock.create(CallMethod::class.java)

        assertThat(obj.hashCode()).isNotZero()
        assertThat(obj == Any()).isFalse()
        assertThat(obj.toString()).isNotEmpty()
    }

    @Test
    fun responseWrappedCall() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://infinum.co/")
            .build()

        val retromock = Retromock.Builder()
            .retrofit(retrofit)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseCallMethod::class.java)

        val responseBody = runBlocking { service.getResponseBody().body() }
        assertThat(responseBody?.string()).isEqualTo("Body example.")
    }

    @Test
    fun cloneSharesStatefulInstances() {
        val bodyFactory = EmptyBodyFactory()
        val defaultBodyFactory = EmptyBodyFactory2()
        val defaultBehavior = mock<Behavior>()
        val backgroundExecutor = mock<ExecutorService>()
        val callbackExecutor = mock<Executor>()

        val first = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
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

    @Test
    fun builtInFactoryAbsentInCloneBuilder() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.bodyFactories()).isNotEmpty
        assertThat(retromock.newBuilder().bodyFactories()).isEmpty()
    }

    @Test
    fun passThroughBodyFactoryAddedByDefault() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.bodyFactories().size).isEqualTo(1)
        assertThat(retromock.bodyFactories().get(PassThroughBodyFactory::class.java)).isNotNull()
    }

    @Test
    fun bodyFactoryNotCalledIfNoResponse() {
        val countDown = AtomicInteger(1)
        val factory = CountDownBodyFactory(countDown)

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(MockedMethod::class.java)

        runBlocking { service.getResponseBody() }

        assertThat(countDown.get()).isEqualTo(1)
    }

    @Test
    fun annotationBodyPassedToBodyFactory() {
        val factory = mock<BodyFactory>()
        whenever(factory.create(Mockito.anyString())).thenReturn(byteArrayOf().inputStream())

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethod::class.java)

        runBlocking { service.getResponseBody() }

        Mockito.verify(factory).create("Body example.")
    }

    @Test
    fun bodyFactoryCalledExactlyOnce() {
        val countDown = AtomicInteger(1)
        val factory = CountDownBodyFactory(countDown)

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethod::class.java)

        runBlocking { service.getResponseBody() }

        assertThat(countDown.get()).isEqualTo(0)
    }

    @Test
    fun bodyFactoryCalledTwice() {
        val countDown = AtomicInteger(2)
        val factory = CountDownBodyFactory(countDown)

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethod::class.java)

        runBlocking { service.getResponseBody() }
        assertThat(countDown.get()).isEqualTo(1)

        runBlocking { service.getResponseBody() }
        assertThat(countDown.get()).isEqualTo(0)
    }

    @Test
    fun bodyFactoryCalledWithCorrectInput() {
        val factory = mock<BodyFactory>()
        whenever(factory.create(Mockito.anyString())).then {
            (it.arguments[0] as String).byteInputStream(StandardCharsets.UTF_8)
        }

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ThreeResponsesMethod::class.java)

        runBlocking { service.getResponseBody() }
        Mockito.verify(factory).create("Body example.")

        runBlocking { service.getResponseBody() }
        Mockito.verify(factory).create("Body example 2.")

        runBlocking { service.getResponseBody() }
        Mockito.verify(factory).create("Body example 3.")

        Mockito.verifyNoMoreInteractions(factory)
    }

    @Test
    fun correctBodyFactoryCalled() {
        val countDown = AtomicInteger(1)
        val factory = CountDownBodyFactory(countDown)

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .addBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethodWithCustomBodyFactory::class.java)

        runBlocking { service.getResponseBody() }

        assertThat(countDown.get()).isEqualTo(0)
    }

    @Test
    fun noMatchingBodyFactoryExceptionThrown() {

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethodWithCustomBodyFactory::class.java)

        assertThrows<IllegalStateException> {
            runBlocking { service.getResponseBody() }
        }
    }

    @Test
    fun defaultBodyFactoryCalledIfNotSpecified() {
        val factory = mock<BodyFactory>()
        whenever(factory.create(Mockito.anyString())).thenReturn("".byteInputStream(StandardCharsets.UTF_8))

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ResponseMethod::class.java)

        runBlocking { service.getResponseBody() }
        Mockito.verify(factory).create("Body example.")

        Mockito.verifyNoMoreInteractions(factory)
    }

    @Test
    fun correctBodyFactoriesCalledInSequence() {
        val countDown = AtomicInteger(1)
        val factory = CountDownBodyFactory(countDown)

        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .addBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

        val service = retromock.create(ThreeResponsesMethodWithCustomBodyFactory::class.java)
        runBlocking {
            val response1 = service.getResponseBody()
            assertThat(response1.string()).isEqualTo("Body example.")

            val response2 = service.getResponseBody()
            assertThat(response2.string()).isEqualTo("Body example 2.")

            val response3 = service.getResponseBody()
            assertThat(response3.string()).isEqualTo("Body example 3.")
            assertThat(countDown.get()).isEqualTo(0)
        }
    }

    @Test
    fun builderInjectsPassThroughBodyFactory() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.bodyFactories()).containsKey(PassThroughBodyFactory::class.java)
    }

    @Test
    fun builderCreatesDefaultExecutorIfNotExplicitlySet() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.backgroundExecutor()).isNotNull()
    }

    @Test
    fun builderUsesExecutorIfExplicitlySet() {
        val backgroundExecutor = Executors.newSingleThreadExecutor()
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .backgroundExecutor(backgroundExecutor)
            .build()

        assertThat(retromock.backgroundExecutor()).isSameAs(backgroundExecutor)
    }

    @Test
    fun builderCreatesDefaultCallbackExecutorIfNotExplicitlySet() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.callbackExecutor()).isInstanceOf(Retromock.SyncExecutor::class.java)
    }

    @Test
    fun builderUsesCallbackExecutorIfExplicitlySet() {
        val callbackExecutor = Executors.newSingleThreadExecutor()
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .callbackExecutor(callbackExecutor)
            .build()

        assertThat(retromock.callbackExecutor()).isSameAs(callbackExecutor)
    }

    @Test
    fun builderUsesRetrofitCallbackExecutorIfNotExplicitlySetInRetromock() {
        val callbackExecutor = Executors.newSingleThreadExecutor()
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .callbackExecutor(callbackExecutor)
                    .build()
            )
            .build()

        assertThat(retromock.callbackExecutor()).isSameAs(callbackExecutor)
    }

    @Test
    fun builderCreatesBehaviorIfNotExplicitlySet() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.defaultBehavior()).isSameAs(DefaultBehavior.INSTANCE)
    }

    @Test
    fun builderCreatesPassThroughBodyFactoryIfDefaultFactoryIsNotSet() {
        val retromock = Retromock.Builder()
            .retrofit(
                Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build()
            )
            .build()

        assertThat(retromock.defaultBodyFactory()).isInstanceOf(PassThroughBodyFactory::class.java)
    }
}

