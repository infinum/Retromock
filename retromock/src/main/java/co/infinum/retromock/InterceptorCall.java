package co.infinum.retromock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;

final class InterceptorCall implements Call {

  private final Request request;
  private final Retromock retromock;
  private final ParamsProducer producer;
  private final Behavior behavior;

  private final AtomicBoolean executed = new AtomicBoolean(false);
  private final CancelInterceptor cancelInterceptor;

  private volatile Future<?> task;

  InterceptorCall(
    final Request request,
    final Retromock retromock,
    final ParamsProducer producer,
    final Behavior behavior
  ) {
    this.request = request;
    this.retromock = retromock;
    this.producer = producer;
    this.behavior = behavior;
    this.cancelInterceptor = new CancelInterceptor();
  }

  @Override
  public Request request() {
    return request;
  }

  @Override
  public Response execute() throws IOException {
    if (executed.getAndSet(true)) {
      throw new IllegalStateException("Already executed.");
    }
    if (isCanceled()) {
      throw new IOException("Canceled");
    }
    Response response = getResponseWithInterceptorChain();
    if (response == null) {
      throw new IOException("Canceled");
    }
    return response;
  }

  @Override
  public void enqueue(final Callback responseCallback) {
    if (executed.getAndSet(true)) {
      throw new IllegalStateException("Already executed.");
    }
    task = retromock.backgroundExecutor().submit(new Runnable() {
      @Override
      public void run() {
        try {
          final Response response = getResponseWithInterceptorChain();
          retromock.callbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
              try {
                responseCallback.onResponse(InterceptorCall.this, response);
              } catch (final IOException e) {
                responseCallback.onFailure(InterceptorCall.this, e);
              }
            }
          });
        } catch (final IOException e) {
          retromock.callbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
              responseCallback.onFailure(InterceptorCall.this, e);
            }
          });
        }
      }
    });
  }

  @Override
  public void cancel() {
    cancelInterceptor.cancel();
    Future<?> task = this.task;
    if (task != null) {
      task.cancel(true);
    }
  }

  @Override
  public boolean isExecuted() {
    return executed.get();
  }

  @Override
  public boolean isCanceled() {
    return cancelInterceptor.isCanceled();
  }

  @Override
  public Call clone() {
    return new InterceptorCall(request, retromock, producer, behavior);
  }

  private Response getResponseWithInterceptorChain() throws IOException {
    List<Interceptor> interceptors = new ArrayList<>();
    // stops request before interceptors
    interceptors.add(cancelInterceptor);

    interceptors.addAll(retromock.interceptors());

    // stops response before interceptors
    interceptors.add(cancelInterceptor);

    // provides a response
    interceptors.add(new ReplyInterceptor(producer, behavior));

    MockChain chain = new MockChain(request, interceptors, this, 0, 0, 0, 0);
    return chain.proceed(request);
  }

  static class MockChain implements Interceptor.Chain {

    private final Request request;
    private final List<Interceptor> interceptors;
    private final Call call;

    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;

    private final int index;

    MockChain(
      final Request request,
      final List<Interceptor> interceptors,
      final Call call,
      final int connectTimeout,
      final int readTimeout,
      final int writeTimeout,
      final int index
    ) {
      this.request = request;
      this.interceptors = interceptors;
      this.call = call;
      this.connectTimeout = connectTimeout;
      this.readTimeout = readTimeout;
      this.writeTimeout = writeTimeout;
      this.index = index;

      if (index > interceptors.size()) {
        throw new IllegalArgumentException(
          "Index " + index + " does not exist in interceptors list " + interceptors + ".");
      }
    }

    @Override
    public Request request() {
      return request;
    }

    @Override
    public Response proceed(final Request request) throws IOException {
      MockChain next = new MockChain(
        request, interceptors, call, connectTimeout, readTimeout, writeTimeout, index + 1
      );

      Interceptor interceptor = interceptors.get(index);
      Response response = interceptor.intercept(next);

      Preconditions.checkNotNull(
        response,
        "Interceptor " + interceptor + " returned null response."
      );

      return response;
    }

    @Nullable
    @Override
    public Connection connection() {
      // no connection for mock calls
      return null;
    }

    @Override
    public Call call() {
      return call;
    }

    @Override
    public int connectTimeoutMillis() {
      return connectTimeout;
    }

    @Override
    public Interceptor.Chain withConnectTimeout(final int timeout, final TimeUnit unit) {
      int millis = Util.checkDuration("timeout", timeout, unit);
      return new MockChain(request, interceptors, call, millis, readTimeout, writeTimeout, index);
    }

    @Override
    public int readTimeoutMillis() {
      return readTimeout;
    }

    @Override
    public Interceptor.Chain withReadTimeout(final int timeout, final TimeUnit unit) {
      int millis = Util.checkDuration("timeout", timeout, unit);
      return new MockChain(request, interceptors, call, connectTimeout, millis, writeTimeout,
        index);
    }

    @Override
    public int writeTimeoutMillis() {
      return writeTimeout;
    }

    @Override
    public Interceptor.Chain withWriteTimeout(final int timeout, final TimeUnit unit) {
      int millis = Util.checkDuration("timeout", timeout, unit);
      return new MockChain(request, interceptors, call, connectTimeout, readTimeout, millis, index);
    }
  }
}
