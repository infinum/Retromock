package co.infinum.samples.retromock;

import com.squareup.moshi.Json;

import java.io.IOException;

import co.infinum.retromock.Retromock;
import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockHeader;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

class InterceptorSample {

  public interface Service {

    @Mock
    @MockResponse(body = "smith.json", headers = {
      @MockHeader(name = "customHeader", value = "user Smith"),
      @MockHeader(name = "Server", value = "google.com")
    })
    @GET("/")
    Call<InterceptorSample.User> getUser();
  }

  static class User {

    @Json(name = "name")
    String name;

    @Json(name = "surname")
    String surname;

    @Override
    public String toString() {
      return "User{"
        + "name='" + name + '\''
        + ", surname='" + surname + '\''
        + '}';
    }
  }

  public static void main(String[] args) throws IOException {

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://www.google.com")
      .addConverterFactory(MoshiConverterFactory.create())
      .build();

    Retromock retromock = new Retromock.Builder()
      .retrofit(retrofit)
      .defaultBodyFactory(new ResourceBodyFactory())
      .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
          .addHeader("interceptorRequestHeader", "custom")
          .build()
        ).newBuilder()
          .addHeader("interceptorResponseHeader", "value")
          .build()
      )
      .build();

    InterceptorSample.Service service = retromock.create(InterceptorSample.Service.class);

    Response<User> response = service.getUser().execute();
    System.out.println("Request headers:");
    System.out.println(response.raw().request().headers());
    System.out.println();
    System.out.println("Response headers:");
    System.out.println(response.headers());
  }
}
