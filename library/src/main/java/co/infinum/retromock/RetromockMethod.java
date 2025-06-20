package co.infinum.retromock;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;

import javax.annotation.Nullable;

import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockBehavior;
import co.infinum.retromock.meta.MockCircular;
import co.infinum.retromock.meta.MockRandom;
import co.infinum.retromock.meta.MockResponse;
import co.infinum.retromock.meta.MockResponseProvider;
import co.infinum.retromock.meta.MockResponses;
import co.infinum.retromock.meta.MockSequential;
import okhttp3.Headers;

final class RetromockMethod {

    /**
     * Default response parameters used when no specific response is configured.
     */
  private static final ResponseParams DEFAULT_PARAMS = new ResponseParams.Builder()
    .code(HttpURLConnection.HTTP_OK)
    .message("OK")
    .headers(new Headers.Builder().add("Content-Type", "text/plain").build())
    .build();

  static RetromockMethod parse(final Method method, final Retromock retromock) throws
    Retromock.DisabledException {
    Mock mock = method.getAnnotation(Mock.class);
    if (mock == null || !mock.value()) {
      throw new Retromock.DisabledException();
    }

    MockResponse[] responses = loadMockResponses(method);
    MockResponseProvider provider = method.getAnnotation(MockResponseProvider.class);
    ParamsProducer producer;
    if (responses != null && provider != null) {
      throw new RuntimeException("Method " + method.getDeclaringClass() + "." + method.getName()
        + " has both @MockResponse and @MockResponseProvider annotations. Retromock supports usage"
        + " of only one of those on a single service method.");
    } else if (responses != null) {
      producer = new ResponseParamsProducer(
        retromock,
        loadResponseIterator(method, responses),
        DEFAULT_PARAMS
      );
    } else if (provider != null) {
      try {
        producer = new ProviderResponseProducer(provider.value(), method, retromock);
      } catch (Exception e) {
        throw new RuntimeException("Cannot create response provider " + provider.value(), e);
      }
    } else {
      producer = new NoResponseProducer(retromock, DEFAULT_PARAMS);
    }

    Behavior behavior = retromock.defaultBehavior();
    MockBehavior mockBehavior = method.getAnnotation(MockBehavior.class);
    if (mockBehavior != null) {
      behavior = new RetromockBehavior(mockBehavior);
    }

    return new RetromockMethod(producer, behavior);
  }

  @Nullable
  private static MockResponse[] loadMockResponses(final Method method) {
    MockResponses mockResponses = method.getAnnotation(MockResponses.class);
    if (mockResponses != null) {
      return mockResponses.value();
    }

    MockResponse mockResponse = method.getAnnotation(MockResponse.class);
    if (mockResponse != null) {
      return new MockResponse[] {mockResponse};
    }

    return null;
  }

  private static ResponseIterator<MockResponse> loadResponseIterator(
    final Method method, final MockResponse[] responses) {

    MockCircular mockCircular = method.getAnnotation(MockCircular.class);
    MockSequential mockSequential = method.getAnnotation(MockSequential.class);
    MockRandom mockRandom = method.getAnnotation(MockRandom.class);

    if (mockCircular != null) {
      if (mockSequential != null || mockRandom != null) {
        throw new IllegalStateException("Cannot specify more than one response iterator.");
      }
      return new CircularIterator<>(responses);
    }

    if (mockSequential != null) {
      if (mockRandom != null) {
        throw new IllegalStateException("Cannot specify more than one response iterator.");
      }
      return new SequentialIterator<>(responses);
    }

    if (mockRandom != null) {
      return new RandomIterator<>(responses);
    }

    return new SequentialIterator<>(responses);
  }

  /**
   * Producer for generating response parameters based on method arguments.
   */
  private final ParamsProducer producer;

  /**
   * Behavior configuration for this mock method.
   */
  private final Behavior behavior;

  private RetromockMethod(
    final ParamsProducer producer,
    final Behavior behavior
  ) {
    this.producer = producer;
    this.behavior = behavior;
  }

  ParamsProducer producer() {
    return producer;
  }

  Behavior behavior() {
    return behavior;
  }
}
