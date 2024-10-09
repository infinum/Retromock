package co.infinum.retromock;

import java.util.HashMap;
import java.util.Map;

import co.infinum.retromock.meta.MockHeader;
import co.infinum.retromock.meta.MockResponse;
import okhttp3.Headers;

final class ResponseParamsProducer implements ParamsProducer {

  private final Retromock retromock;
  private final ResponseIterator<MockResponse> iterator;
  private final Map<MockResponse, ResponseParams> cache;
  private final ResponseParams defaults;

  ResponseParamsProducer(
    final Retromock retromock,
    final ResponseIterator<MockResponse> iterator,
    final ResponseParams defaults) {

    this.retromock = retromock;
    this.iterator = iterator;
    this.defaults = defaults;
    this.cache = new HashMap<>();
  }

  @Override
  public ResponseParams produce(final Object[] args) {
    MockResponse mockResponse = iterator.next();
    ResponseParams params = cache.get(mockResponse);
    if (params != null) {
      return params;
    }

    ResponseParams.Builder builder = defaults.newBuilder();
    parseResponseAnnotation(builder, mockResponse, retromock);
    params = builder.build();

    cache.put(mockResponse, params);
    return params;
  }

  private static void parseResponseAnnotation(
    final ResponseParams.Builder builder,
    final MockResponse annotation,
    final Retromock retromock) {

    builder.code(annotation.code())
      .message(annotation.message())
      .headers(convertHeaders(annotation.headers()))
      .bodyFactory(new RetromockBodyFactory(retromock.bodyFactory(annotation.bodyFactory()),
        annotation.body()));
  }

  private static Headers convertHeaders(final MockHeader[] headers) {
    Headers.Builder builder = new Headers.Builder();
    for (MockHeader header : headers) {
      builder.add(header.name(), header.value());
    }
    return builder.build();
  }
}
