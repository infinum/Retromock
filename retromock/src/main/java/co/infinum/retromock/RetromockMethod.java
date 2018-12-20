package co.infinum.retromock;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;

import javax.annotation.Nullable;

import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockBehavior;
import co.infinum.retromock.meta.MockCircular;
import co.infinum.retromock.meta.MockRandom;
import co.infinum.retromock.meta.MockResponse;
import co.infinum.retromock.meta.MockResponses;
import co.infinum.retromock.meta.MockSequential;
import okhttp3.Headers;

final class RetromockMethod {

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
    ParamsProducer producer;
    if (responses != null) {
      producer = new ResponseParamsProducer(
        retromock,
        loadResponseIterator(method, responses),
        DEFAULT_PARAMS
      );
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
  private static MockResponse[] loadMockResponses(Method method) {
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
    Method method, MockResponse[] responses) {

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

  private final ParamsProducer producer;
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
