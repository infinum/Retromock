package co.infinum.retromock;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Okio;

class ReplyInterceptor implements Interceptor {

  private final RetromockMethod method;

  ReplyInterceptor(final RetromockMethod method) {
    this.method = method;
  }

  private void delay() throws InterruptedException {
    long delayMillis = method.behavior().delayMillis();
    if (delayMillis > 0) {
      Thread.sleep(delayMillis);
    }
  }

  @Override
  public okhttp3.Response intercept(final Chain chain) throws IOException {
    try {
      delay();
    } catch (InterruptedException interrupt) {
      throw new IOException("canceled");
    }
    ResponseParams params = method.producer().produce();

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
