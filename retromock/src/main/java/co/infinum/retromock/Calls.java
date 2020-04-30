package co.infinum.retromock;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class Calls {

  /**
   * Invokes {@code callable} once for the returned {@link Call} and once for each instance that is
   * obtained from {@linkplain Call#clone() cloning} the returned {@link Call}.
   */
  static <T> Call<T> defer(final Callable<Call<T>> callable) {
    return new DeferredCall<>(callable);
  }

  static <T> Call<T> response(final T successValue) {
    return new FakeCall<>(Response.success(successValue), null);
  }

  static <T> Call<T> response(final Response<T> response) {
    return new FakeCall<>(response, null);
  }

  static <T> Call<T> failure(final IOException failure) {
    return new FakeCall<>(null, failure);
  }

  private Calls() {
  }

  static final class FakeCall<T> implements Call<T> {

    private final Response<T> response;
    private final IOException error;
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final AtomicBoolean executed = new AtomicBoolean();

    FakeCall(final @Nullable Response<T> response, final @Nullable IOException error) {
      if ((response == null) == (error == null)) {
        throw new AssertionError("Only one of response or error can be set.");
      }
      this.response = response;
      this.error = error;
    }

    @Override
    public Response<T> execute() throws IOException {
      if (!executed.compareAndSet(false, true)) {
        throw new IllegalStateException("Already executed");
      }
      if (canceled.get()) {
        throw new IOException("canceled");
      }
      if (response != null) {
        return response;
      }
      assert error != null;
      throw error;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
      if (!executed.compareAndSet(false, true)) {
        throw new IllegalStateException("Already executed");
      }
      if (canceled.get()) {
        callback.onFailure(this, new IOException("canceled"));
      } else if (response != null) {
        callback.onResponse(this, response);
      } else {
        assert error != null;
        callback.onFailure(this, error);
      }
    }

    @Override
    public boolean isExecuted() {
      return executed.get();
    }

    @Override
    public void cancel() {
      canceled.set(true);
    }

    @Override
    public boolean isCanceled() {
      return canceled.get();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Call<T> clone() {
      return new FakeCall<>(response, error);
    }

    @Override
    public Request request() {
      if (response != null) {
        return response.raw().request();
      }
      return new Request.Builder().url("http://localhost").build();
    }

    @Override
    public Timeout timeout() {
        return new Timeout();
    }
  }

  static final class DeferredCall<T> implements Call<T> {

    private final Callable<Call<T>> callable;
    private Call<T> delegate;

    DeferredCall(final Callable<Call<T>> callable) {
      this.callable = callable;
    }

    private synchronized Call<T> getDelegate() {
      Call<T> delegate = this.delegate;
      if (delegate == null) {
        try {
          delegate = callable.call();
        } catch (IOException e) {
          delegate = failure(e);
        } catch (Exception e) {
          throw new IllegalStateException("Callable threw unrecoverable exception", e);
        }
        this.delegate = delegate;
      }
      return delegate;
    }

    @Override
    public Response<T> execute() throws IOException {
      return getDelegate().execute();
    }

    @Override
    public void enqueue(final Callback<T> callback) {
      getDelegate().enqueue(callback);
    }

    @Override
    public boolean isExecuted() {
      return getDelegate().isExecuted();
    }

    @Override
    public void cancel() {
      getDelegate().cancel();
    }

    @Override
    public boolean isCanceled() {
      return getDelegate().isCanceled();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Call<T> clone() {
      return new DeferredCall<>(callable);
    }

    @Override
    public Request request() {
      return getDelegate().request();
    }

    @Override
    public Timeout timeout() {
      return delegate.timeout();
    }
  }
}
