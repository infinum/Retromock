Service declaration
-------

#### `@Mock`
Use this annotation on a service method when you want to specify that the method should be mocked. It is possible to enable or disable method mock with the `value` parametar of type `boolean`.

If annotation is not listed on a method or is disabled with the `value` parameter then the method will not be mocked.
###### Example
```java
  @GET("/endpoint")
  Call<User> getUser();
``` 
```java
  @Mock(false)
  @GET("/endpoint")
  Call<User> getUser();
``` 

Annotating the method without a `value` parameter and explicitly setting it to `true` will both result in mocked response since `true` is the default value.
###### Example
```java
  @Mock
  @GET("/endpoint")
  Call<User> getUser();
``` 
```java
  @Mock(true)
  @GET("/endpoint")
  Call<User> getUser();
``` 

#### `@MockResponse`
Use this annotation to specify parameters for defining a mocked response.

If annotation is not present mocked response defaults to `200 OK` with an empty body and no headers.

Parameters and default values:
 - `code` - `200`
 - `message` - `OK`
 - `body` - _empty_
 - `headers` - `[]`
 - `bodyFactory` - _default body factory set in Retromock instance_

###### Example
```java
  @Mock
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Doe\"}")
  @GET("/endpoint")
  Call<User> getUser();
```

Headers can be declared as array of type `@MockHeader` (`name-value` pair) annotations.
###### Example
```java
  @Mock
  @MockResponse(
    body = "{\"name\":\"John\", \"surname\":\"Smith\"}",
    headers = {
      @MockHeader(name = "Content-Type", value = "application/json"),
      @MockHeader(name = "AnyCustomHeader", value = "AnyCustomValue")
  })
  @GET("/endpoint")
  Call<User> getUser();
```

Methods can have multiple `@MockResponse` annotations.
###### Java example
```java
  @Mock
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Smith\"}")
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Doe\"}")
  @GET("/endpoint")
  Call<User> getUser();
```
###### Kotlin example
```kotlin
  @Mock
  @MockResponses(
    MockResponse(body = "Body example."),
    MockResponse(body = "Body example 2."),
    MockResponse(body = "Body example 3.")
  )
  @GET("/endpoint")
  suspend fun getResponseBody(): ResponseBody?
```
By default, Retromock returns responses in the same order as annotations defined.

For example, let's say that service method is annotated with 3 responses: `[one, two, three]`.
If you enqueue the service method 5 times it will produce `one, two, three, three, three`.

To alter that behavior, use either
 - `@MockCircular` - produces `[one, two, three, one, two]`, or
 - `@MockRandom` - produces one of responses in uniform random distribution

Custom implementations are not yet supported.

#### `@MockBehavior`
Use this annotation to define response delay. It accepts mean (`durationMillis)` and deviation (`durationDeviation`) in milliseconds.
Retromock will produce random delay in range 
```
[durationMillis - durationDeviation, durationMillis + durationDeviation]
```
Following example will produce random duration between `500ms` and `1500ms`.
###### Example
```java
  @Mock
  @MockBehavior(durationDeviation = 500, durationMillis = 1000)
  @GET("/endpoint")
  Call<User> getUser();
```

To produce a constant delay set `durationDeviation` parameter to zero. If not specifically set, Retromock will use defaults equivalent to
```java
  @MockBehavior(durationDeviation = 500, durationMillis = 1000)
```

#### `@MockResponseProvider`

Use this annotation to provide a class that has the ability to dynamically generate mock responses.
This is useful when you need to create responses based on method parameters or when you need more
complex logic for generating responses.

The provider class should have exactly one method with the same signature as the service method,
except the return type should be `Response`. The method must be annotated with `@ProvidesMock`.

If a service method is annotated with `@MockResponseProvider`, Retromock will use it for each mock
call to provide a response. Note: either `@MockResponseProvider` or `@MockResponse` annotation(s)
should be used on a single service method, not both.

###### Example with single argument

```java
// Service method
@Mock
@MockResponseProvider(UserProvider.class)
@GET("/users")
Call<User> getUser(@Query("id") String userId);

// Provider class
public class UserProvider {
  @ProvidesMock
  public Response getUser(String userId) {
    String body = "{\"id\":\"" + userId + "\", \"name\":\"User " + userId + "\"}";
    return new Response.Builder()
        .body(body)
        .build();
  }
}
```

###### Example with multiple arguments

```java
// Service method
@Mock
@MockResponseProvider(SearchProvider.class)
@GET("/search")
Call<SearchResult> search(@Query("query") String query, @Query("limit") int limit);

// Provider class
public class SearchProvider {
  @ProvidesMock
  public Response search(String query, int limit) {
    String body = "{\"query\":\"" + query + "\", \"limit\":" + limit + ", \"results\":[]}";
    return new Response.Builder()
        .code(200)
        .message("OK")
        .body(body)
        .build();
  }
}
```

Retromock declaration
-------
#### `BodyFactory`

By default `Retromock` mocks response body with the exact string provided in the `body` parameter of `@MockResponse` annotation.
In order to provide a response body from a custom source create a `BodyFactory` implementation and set it with the `bodyFactory` parametar.
###### Example
1. Create an implementation of `BodyFactory` that loads a stream using application class loader.
```java
class ResourceBodyFactory implements BodyFactory {

  @Override
  public InputStream create(final String input) throws IOException {
    return ResourceBodyFactory.class.getClassLoader().getResourceAsStream(input);
  }
}
```
In Android, `BodyFactory` can load a response body from asset:
```java
class AssetBodyFactory implements BodyFactory {

  private final AssetManager assetManager;

  public AssetBodyFactory(final AssetManager assetManager) {
    this.assetManager = assetManager;
  }

  @Override
  public InputStream create(final String input) throws IOException {
    return assetManager.open(input);
  }
}
```
Or use any other implementation that suits your needs.

2. Create a `Retromock` instance and add a body factory. Note: Body factory class cannot be annonymous class because it is referenced later on.
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .addBodyFactory(new ResourceBodyFactory())
  .build();
```
3. Set body factory implementation class with `bodyFactory` parameter.
```java
public interface Service {

  @Mock
  @MockResponse(body = "smith.json", bodyFactory = ResourceBodyFactory.class)
  @GET("/")
  Call<User> getUser();
}
```

If majority of your service methods use a particular body factory you can set it as default.
If so, `bodyFactory` parameter does not have to be specified.
In this case `BodyFactory` instance can be annonymous class.

###### Example
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(new ResourceBodyFactory())
  .build();
```
No need to set a body factory - default one will be used if non is provided.
```java
public interface Service {

  @Mock
  @MockResponse(body = "smith.json")
  @GET("/")
  Call<User> getUser();
}
```

Note: if you set custom default body factory and do not declare a `bodyFactory` parameter in `@MockResponse` annotation your body factory will be called with value of `body` parameter.

That also applies if you don't specificaly set `body` - in that case `body` is empty by default.
If you wouldn't like to handle the case of empty `body` wrap your default body factory into `NonEmptyBodyFactory` class as follows:
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(new NonEmptyBodyFactory(...))
  .build();
```

#### `Behavior`
Implementation of this class provides a response delay in milliseconds.

If you want to set a custom default delay implement this class and set it as `defaultBehavior` in the builder.

If so, this behavior will be used on all service methods that do not have `@MockBehavior` annotation.

If not set, Retromock uses default behavior that produces response delays randomly in uniform distribution between `500ms` and `1500ms`.
###### Example
Remove response delay
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBehavior(() -> 0)
  .build();
```

#### Executors
You can set background and callback executors in `Retromock` builder.

By default, background executor is set to mock a call on background thread.
If you set a custom implementation keep in mind that response delay will block the thread.

Retromock by default uses callback executor from `Retrofit` instance.
If you want a custom one, feel free to set it using the builder.

#### `create`
To create an implementation of your service call `create` method.
There are two overloads of `create` method:

 - `create(DelegateFactory<T> factory, Class<T> service)` creates a service of class `service`.
`factory` is used to create a delegate service to which non-mocked calls will be redirected.

 - `create(Class<T> service)` creates a service of class `service` and delegates non-mocked calls to a service created by `Retrofit` instance.

#### Call adapters and Converters
There is no limit in usage of call adapters and converters - `Retromock` delegates parsing and adapting to `Retrofit`. Whatever works for `Retrofit` will work for `Retromock` too.
