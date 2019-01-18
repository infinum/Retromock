package co.infinum.samples.retromock;

import com.squareup.moshi.Json;

import java.io.FileInputStream;
import java.io.IOException;

import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.NonEmptyBodyFactory;
import co.infinum.retromock.Retromock;
import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

class DefaultBodyFactoryHandleEmptyBody {

  public interface Service {

    @Mock
    @MockResponse(body = "smith.json")
    @GET("/")
    Call<DefaultBodyFactoryHandleEmptyBody.User> getUser();

    @Mock
    @MockResponse(code = 400)
    @GET("/")
    Call<User> getOtherUser();
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

    BodyFactory readFromResourcesBodyFactory = input -> {
      // this will throw if input is empty string, regular class loader opens a stream to directory
      return new FileInputStream(
        DefaultBodyFactoryHandleEmptyBody.class.getClassLoader().getResource(input).getFile()
      );
    };

    Retromock retromock = new Retromock.Builder()
      .retrofit(retrofit)
      .defaultBodyFactory(new NonEmptyBodyFactory(readFromResourcesBodyFactory))
      .build();

    DefaultBodyFactoryHandleEmptyBody.Service service =
      retromock.create(DefaultBodyFactoryHandleEmptyBody.Service.class);

    System.out.println(service.getUser().execute().body());
    System.out.println(service.getOtherUser().execute());
  }
}
