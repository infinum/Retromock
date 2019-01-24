package co.infinum.retromock;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Response;

final class CancelInterceptor implements Interceptor {

  private final AtomicBoolean canceled = new AtomicBoolean(false);

  @Override
  public Response intercept(final Chain chain) throws IOException {
    if (canceled.get()) {
      throw new IOException("canceled");
    }
    Response response = chain.proceed(chain.request());
    assert response.body() != null;
    if (canceled.get()) {
      throw new IOException("canceled");
    }
    return response;
  }

  void cancel() {
    canceled.set(true);
  }

  boolean isCanceled() {
    return canceled.get();
  }

}
