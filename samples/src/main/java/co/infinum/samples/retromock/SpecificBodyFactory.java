package co.infinum.samples.retromock;

import com.squareup.moshi.Json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.Retromock;
import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

class SpecificBodyFactory {

  static class CustomBuildBodyFactory implements BodyFactory {

    @Override
    public InputStream create(final String input) throws IOException {
      String[] args = input.split("\\.");
      return new ByteArrayInputStream(
        ("{\"name\":\"" + args[0] + "\", \"surname\":\"" + args[1] + "\"}")
          .getBytes(StandardCharsets.UTF_8)
      );
    }
  }

  public interface Service {

    @Mock
    @MockResponse(body = "smith.json")
    @GET("/")
    Call<SpecificBodyFactory.User> getUser();

    @Mock
    @MockResponse(
      body = "John.Doe",
      bodyFactory = CustomBuildBodyFactory.class
    )
    @GET("/")
    Call<SpecificBodyFactory.User> userMockedDirectlyInAnnotation();
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
      .addBodyFactory(new CustomBuildBodyFactory())
      .defaultBodyFactory(new ResourceBodyFactory())
      .build();

    SpecificBodyFactory.Service service = retromock.create(SpecificBodyFactory.Service.class);

    System.out.println(service.getUser().execute().body());
    System.out.println(service.userMockedDirectlyInAnnotation().execute().body());
  }
}
