package co.infinum.retromock;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Okio;

final class ReplyInterceptor implements Interceptor {

  private final ParamsProducer producer;
  private final Behavior behavior;

  ReplyInterceptor(final ParamsProducer producer, final Behavior behavior) {
    this.producer = producer;
    this.behavior = behavior;
  }

  private void delay(final int maxDelay) throws InterruptedException {
    long delayMillis = behavior.delayMillis();
    if (maxDelay > 0) {
      delayMillis = Math.min(delayMillis, maxDelay);
    }
    if (delayMillis > 0) {
      Thread.sleep(delayMillis);
    }
  }

  @Override
  public okhttp3.Response intercept(final Chain chain) throws IOException {
    try {
      delay(chain.connectTimeoutMillis());
    } catch (InterruptedException interrupt) {
      throw new SocketTimeoutException(
        "Timeout occurred - mock behavior delay is greater than connect timeout");
    }
    ResponseParams params = producer.produce();

    RetromockBodyFactory factory = params.bodyFactory();

    ResponseBody responseBody;
    if (factory != null) {

      String contentType = params.contentType();
      MediaType mediaType = null;
      if (contentType != null) {
        mediaType = MediaType.parse(contentType);
      }

      responseBody = ResponseBody.create(
        mediaType,
        params.contentLength(),
        Okio.buffer(Okio.source(factory.createBody())));
    } else {
      responseBody = Util.EMPTY_RESPONSE;
    }

    return new okhttp3.Response.Builder()
      .code(params.code())
      .message(params.message())
      .body(responseBody)
      .protocol(Protocol.HTTP_1_1)
      .headers(params.headers())
      .request(chain.request())
      .build();
  }
}
