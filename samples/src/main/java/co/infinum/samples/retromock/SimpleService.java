package co.infinum.samples.retromock;

import com.squareup.moshi.Json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import co.infinum.retromock.Behavior;
import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.Retromock;
import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

public final class SimpleService {

  public static void main(String[] args) throws IOException {

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://www.google.com")
      .addConverterFactory(MoshiConverterFactory.create())
      .build();

    Retromock retromock = new Retromock.Builder()
      .retrofit(retrofit)
      .defaultBehavior(new Behavior() {
        @Override
        public long delayMillis() {
          return 0;
        }
      })
      .addBodyFactory(new RandomBodyFactory())
      .build();

    Service service = retromock.create(Service.class);

    System.out.println();
    System.out.println("User:");
    System.out.println(service.getUser().execute().body());
    System.out.println("User:");
    System.out.println(service.getUser().execute().body());
    System.out.println("User:");
    System.out.println(service.getUser().execute().body());
    System.out.println("User:");
    System.out.println(service.getUser().execute().body());

    System.out.println(service.def().execute().body().string());
  }

  static class RandomBodyFactory implements BodyFactory {

    private final Random random = new Random();

    @Override
    public InputStream create(final String input) {
      int contentLength = random.nextInt(1024);
      byte[] bytes = new byte[contentLength];
      random.nextBytes(bytes);
      return new ByteArrayInputStream(bytes);
    }

  }

  public interface Service {

    @Mock
    @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Smith\"}")
    @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Doe\"}")
    @GET("/")
    Call<User> getUser();

    @Mock
    default Call<ResponseBody> def() { return null; }
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
}
