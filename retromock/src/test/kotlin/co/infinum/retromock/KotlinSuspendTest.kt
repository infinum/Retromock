package co.infinum.retromock

import co.infinum.retromock.helpers.*
import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import co.infinum.retromock.meta.MockResponses
import co.infinum.retromock.meta.MockSequential
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Java6Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import retrofit2.Retrofit
import retrofit2.http.GET
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
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    val obj = retromock.create(RetromockTest.CallMethod::class.java)

    assertThat(obj.hashCode()).isNotZero()
    Java6Assertions.assertThat(obj == Any()).isFalse()
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

    Java6Assertions.assertThat(first.bodyFactories().size).isEqualTo(second.bodyFactories().size - 1)
    Java6Assertions.assertThat(first.bodyFactories()).contains(
            Java6Assertions.entry(EmptyBodyFactory::class.java, bodyFactory)
    )

    Java6Assertions.assertThat(second.bodyFactories()).contains(
            Java6Assertions.entry(EmptyBodyFactory::class.java, bodyFactory),
            Java6Assertions.entry(EmptyBodyFactory2::class.java, bodyFactory2)
    )

    Java6Assertions.assertThat(first.defaultBodyFactory()).isSameAs(second.defaultBodyFactory())
    Java6Assertions.assertThat(first.defaultBehavior()).isSameAs(second.defaultBehavior())
    Java6Assertions.assertThat(first.callbackExecutor()).isSameAs(second.callbackExecutor())
    Java6Assertions.assertThat(first.backgroundExecutor()).isSameAs(second.backgroundExecutor())

  }

  @Test
  fun builtInFactoryAbsentInCloneBuilder() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.bodyFactories()).isNotEmpty
    Java6Assertions.assertThat(retromock.newBuilder().bodyFactories()).isEmpty()
  }

  @Test
  fun passThroughBodyFactoryAddedByDefault() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.bodyFactories().size).isEqualTo(1)
    Java6Assertions.assertThat(retromock.bodyFactories().get(PassThroughBodyFactory::class.java)).isNotNull()
  }

  @Test
  fun bodyFactoryNotCalledIfNoResponse() {
    val countDown = AtomicInteger(1)
    val factory = CountDownBodyFactory(countDown)

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.MockedMethod::class.java)

    service.getResponseBody().execute()

    Java6Assertions.assertThat(countDown.get()).isEqualTo(1)
  }

  @Test
  fun annotationBodyPassedToBodyFactory() {
    val factory = mock<BodyFactory>()
    whenever(factory.create(Mockito.anyString())).thenReturn(byteArrayOf().inputStream())

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethod::class.java)

    service.getResponseBody().execute()

    Mockito.verify(factory).create("Body example.")
  }

  @Test
  fun bodyFactoryCalledExactlyOnce() {
    val countDown = AtomicInteger(1)
    val factory = CountDownBodyFactory(countDown)

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethod::class.java)

    service.getResponseBody().execute()

    Java6Assertions.assertThat(countDown.get()).isEqualTo(0)
  }

  @Test
  fun bodyFactoryCalledTwice() {
    val countDown = AtomicInteger(2)
    val factory = CountDownBodyFactory(countDown)

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethod::class.java)

    service.getResponseBody().execute()
    Java6Assertions.assertThat(countDown.get()).isEqualTo(1)

    service.getResponseBody().execute()
    Java6Assertions.assertThat(countDown.get()).isEqualTo(0)
  }

  @Test
  fun bodyFactoryCalledWithCorrectInput() {
    val factory = mock<BodyFactory>()
    whenever(factory.create(Mockito.anyString())).then {
      (it.arguments[0] as String).byteInputStream(StandardCharsets.UTF_8)
    }

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ThreeResponsesMethod::class.java)

    service.getResponseBody().execute()
    Mockito.verify(factory).create("Body example.")

    service.getResponseBody().execute()
    Mockito.verify(factory).create("Body example 2.")

    service.getResponseBody().execute()
    Mockito.verify(factory).create("Body example 3.")

    Mockito.verifyNoMoreInteractions(factory)
  }

  @Test
  fun correctBodyFactoryCalled() {
    val countDown = AtomicInteger(1)
    val factory = CountDownBodyFactory(countDown)

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .addBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethodWithCustomBodyFactory::class.java)

    service.getResponseBody().execute()

    Java6Assertions.assertThat(countDown.get()).isEqualTo(0)
  }

  @Test
  fun noMatchingBodyFactoryExceptionThrown() {

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethodWithCustomBodyFactory::class.java)

    assertThrows<IllegalStateException> {
      service.getResponseBody().execute()
    }
  }

  @Test
  fun defaultBodyFactoryCalledIfNotSpecified() {
    val factory = mock<BodyFactory>()
    whenever(factory.create(Mockito.anyString())).thenReturn("".byteInputStream(StandardCharsets.UTF_8))

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .defaultBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ResponseMethod::class.java)

    service.getResponseBody().execute()
    Mockito.verify(factory).create("Body example.")

    Mockito.verifyNoMoreInteractions(factory)
  }

  @Test
  fun correctBodyFactoriesCalledInSequence() {
    val countDown = AtomicInteger(1)
    val factory = CountDownBodyFactory(countDown)

    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .addBodyFactory(factory)
            .defaultBehavior(ImmediateBehavior())
            .build()

    val service = retromock.create(RetromockTest.ThreeResponsesMethodWithCustomBodyFactory::class.java)

    val response1 = service.getResponseBody().execute().body()!!
    assertThat(response1.string()).isEqualTo("Body example.")

    val response2 = service.getResponseBody().execute().body()!!
    assertThat(response2.string()).isEqualTo("Body example 2.")

    val response3 = service.getResponseBody().execute().body()!!
    assertThat(response3.string()).isEqualTo("Body example 3.")
    Java6Assertions.assertThat(countDown.get()).isEqualTo(0)
  }

  @Test
  fun builderInjectsPassThroughBodyFactory() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.bodyFactories()).containsKey(PassThroughBodyFactory::class.java)
  }

  @Test
  fun builderCreatesDefaultExecutorIfNotExplicitlySet() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.backgroundExecutor()).isNotNull()
  }

  @Test
  fun builderUsesExecutorIfExplicitlySet() {
    val backgroundExecutor = Executors.newSingleThreadExecutor()
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .backgroundExecutor(backgroundExecutor)
            .build()

    Java6Assertions.assertThat(retromock.backgroundExecutor()).isSameAs(backgroundExecutor)
  }

  @Test
  fun builderCreatesDefaultCallbackExecutorIfNotExplicitlySet() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.callbackExecutor()).isInstanceOf(Retromock.SyncExecutor::class.java)
  }

  @Test
  fun builderUsesCallbackExecutorIfExplicitlySet() {
    val callbackExecutor = Executors.newSingleThreadExecutor()
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .callbackExecutor(callbackExecutor)
            .build()

    Java6Assertions.assertThat(retromock.callbackExecutor()).isSameAs(callbackExecutor)
  }

  @Test
  fun builderUsesRetrofitCallbackExecutorIfNotExplicitlySetInRetromock() {
    val callbackExecutor = Executors.newSingleThreadExecutor()
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .callbackExecutor(callbackExecutor)
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.callbackExecutor()).isSameAs(callbackExecutor)
  }

  @Test
  fun builderCreatesBehaviorIfNotExplicitlySet() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.defaultBehavior()).isSameAs(DefaultBehavior.INSTANCE)
  }

  @Test
  fun builderCreatesPassThroughBodyFactoryIfDefaultFactoryIsNotSet() {
    val retromock = Retromock.Builder()
            .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co/")
                    .build())
            .build()

    Java6Assertions.assertThat(retromock.defaultBodyFactory()).isInstanceOf(PassThroughBodyFactory::class.java)
  }
}

