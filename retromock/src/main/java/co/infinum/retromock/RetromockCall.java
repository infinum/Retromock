package co.infinum.retromock;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class RetromockCall<T> implements Call<T> {

  private final Behavior behavior;
  private final ExecutorService backgroundExecutor;
  private final Executor callbackExecutor;
  private final Call<T> delegate;

  private volatile Future<?> task;
  private AtomicBoolean canceled;
  private AtomicBoolean executed;

  RetromockCall(
    final Behavior behavior,
    final ExecutorService backgroundExecutor,
    final Executor callbackExecutor,
    final Call<T> delegate) {

    this.behavior = behavior;
    this.backgroundExecutor = backgroundExecutor;
    this.callbackExecutor = callbackExecutor;
    this.delegate = delegate;

    this.canceled = new AtomicBoolean();
    this.executed = new AtomicBoolean();
  }

  @Override
  public Request request() {
    return delegate.request();
  }

  private void enqueueInBackground(final Callback<T> callback) {
    Preconditions.checkNotNull(callback, "Callback is null");

    if (!executed.compareAndSet(false, true)) {
      throw new IllegalStateException("Callback has already been executed!");
    }

    task = backgroundExecutor.submit(new Runnable() {

      private void delay() throws InterruptedException {
        long delayMillis = behavior.delayMillis();
        if (delayMillis > 0) {
          Thread.sleep(delayMillis);
        }
      }

      @Override
      public void run() {
        if (canceled.get()) {
          callback.onFailure(RetromockCall.this, new IOException("canceled"));
        } else {
          try {
            try {
              delay();
            } catch (InterruptedException interrupt) {
              callback.onFailure(RetromockCall.this, new IOException("canceled"));
            }
            delegate.enqueue(new Callback<T>() {
              @Override
              public void onResponse(final Call<T> call, final Response<T> response) {
                try {
                  callback.onResponse(call, response);
                } catch (Throwable throwable) {
                  callback.onFailure(RetromockCall.this, new IOException("canceled"));
                }
              }

              @Override
              public void onFailure(final Call<T> call, final Throwable t) {
                try {
                  callback.onFailure(call, t);
                } catch (Throwable throwable) {
                  callback.onFailure(RetromockCall.this, new IOException("canceled"));
                }
              }
            });
          } catch (Throwable error) {
            callback.onFailure(RetromockCall.this, error);
          }
        }
      }
    });
  }

  @Override
  public void enqueue(final Callback<T> callback) {
    enqueueInBackground(new Callback<T>() {
      @Override
      public void onResponse(final Call<T> call, final Response<T> response) {
        callbackExecutor.execute(new Runnable() {
          @Override
          public void run() {
            callback.onResponse(call, response);
          }
        });
      }

      @Override
      public void onFailure(final Call<T> call, final Throwable t) {
        callbackExecutor.execute(new Runnable() {
          @Override
          public void run() {
            callback.onFailure(call, t);
          }
        });
      }
    });
  }

  @Override
  public Response<T> execute() throws IOException {
    final AtomicReference<Response<T>> responseRef = new AtomicReference<>();
    final AtomicReference<Throwable> errorRef = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);

    enqueueInBackground(new Callback<T>() {
      @Override
      public void onResponse(final Call<T> call, final Response<T> response) {
        responseRef.set(response);
        latch.countDown();
      }

      @Override
      public void onFailure(final Call<T> call, final Throwable t) {
        errorRef.set(t);
        latch.countDown();
      }
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IOException("canceled");
    }

    Response<T> response = responseRef.get();
    if (response != null) {
      return response;
    }

    Throwable error = errorRef.get();
    if (error instanceof RuntimeException) {
      throw (RuntimeException) error;
    } else if (error instanceof IOException) {
      throw (IOException) error;
    } else {
      throw new RuntimeException(error);
    }
  }

  @Override
  public boolean isExecuted() {
    return executed.get();
  }

  @Override
  public void cancel() {
    canceled.set(true);
    Future<?> task = this.task;
    if (task != null) {
      task.cancel(true);
    }
  }

  @Override
  public boolean isCanceled() {
    return canceled.get();
  }

  @SuppressWarnings("CloneDoesntCallSuperClone")
  @Override
  public Call<T> clone() {
    return new RetromockCall<>(behavior, backgroundExecutor, callbackExecutor, delegate);
  }
}
