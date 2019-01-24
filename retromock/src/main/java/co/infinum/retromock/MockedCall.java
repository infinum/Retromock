package co.infinum.retromock;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

class MockedCall<T> implements Call<T> {

  private final Converter<ResponseBody, T> converter;
  private final okhttp3.Call rawCall;

  MockedCall(final Converter<ResponseBody, T> converter, final okhttp3.Call rawCall) {
    this.converter = converter;
    this.rawCall = rawCall;
  }

  @Override
  public Response<T> execute() throws IOException {
    return convertResponse(rawCall.execute());
  }

  @Override
  public void enqueue(final Callback<T> callback) {
    rawCall.enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(final okhttp3.Call call, final IOException error) {
        callback.onFailure(MockedCall.this, error);
      }

      @Override
      public void onResponse(final okhttp3.Call call, final okhttp3.Response response) {
        try {
          callback.onResponse(MockedCall.this, convertResponse(response));
        } catch (Throwable error) {
          callback.onFailure(MockedCall.this, error);
        }
      }
    });
  }

  private Response<T> convertResponse(final okhttp3.Response rawResponse) {
    assert rawResponse.body() != null;
    if (!rawResponse.isSuccessful()) {
      return Response.error(rawResponse.body(), rawResponse);
    } else {
      try {
        T body = null;
        if (rawResponse.code() != HttpURLConnection.HTTP_NO_CONTENT
          && rawResponse.code() != HttpURLConnection.HTTP_RESET) {
          body = converter.convert(rawResponse.body());
        } else {
          // 204 and 205 must not include a body
          rawResponse.close();
        }
        return Response.success(body, rawResponse);
      } catch (IOException e) {
        throw new RuntimeException("Error while converting mocked response!", e);
      }
    }
  }

  @Override
  public boolean isExecuted() {
    return rawCall.isExecuted();
  }

  @Override
  public void cancel() {
    rawCall.cancel();
  }

  @Override
  public boolean isCanceled() {
    return rawCall.isCanceled();
  }

  @SuppressWarnings("CloneDoesntCallSuperClone")
  @Override
  public Call<T> clone() {
    return new MockedCall<>(converter, rawCall);
  }

  @Override
  public Request request() {
    return rawCall.request();
  }
}
