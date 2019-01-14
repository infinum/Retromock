# Retromock

Adapts Java interface created by Retrofit using annotations on declared methods to define response mock.

Quick guide
-------

#### Add dependency
```gradle
implementation 'co.infinum:retromock:0.0.1'
```

#### Initialize
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .build();
```

#### Create a service class
```java
Service service = retromock.create(Service.class);
```

#### Setup mocks
```java
public interface Service {

  @Mock
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Doe\"}")
  @GET("/endpoint")
  Call<User> getUser();
}
```

#### Use the service
```java
Call<User> = service.getUser();
```

Service declaration
-------

#### `@Mock`
Use this annotation on a service method when you want to specify if this method should be mocked.

If annotation is not listed on a method or it is, but with value set to `false` method would not be mocked.
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

On the other hand, if annotation is present on a method and its value is set to `true` response will be mocked.
Adding this annotation without value parameter is equivalent to adding it with `true` parameter.
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
Use this annotation to specify parameters to define a mocked response.

If annotation is not present mocked response would be `200 OK` with empty body and no headers.

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
  @Call<User> getUser();
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
  fun getResponseBody(): Call<ResponseBody>
```
By default, Retromock returns responses in same order as annotations are ordered.

For example, let's say that service method is annotated with 3 responses: `[one, two, three]`.
If you enqueue the service method 5 times it would produce `one, two, three, three, three`.

To alter that behavior, use either
 - `@MockCircular` - produces `[one, two, three, one, two]`, or
 - `@MockRandom` - produces one of responses in uniform random distribution

Any custom implementation isn't supported yet.

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
  @MockBehavior(durationDeviation = 1000, durationMillis = 500)
  @GET("/endpoint")
  Call<User> getUser();
```

To produce a constant delay set `durationDeviation` parameter to zero. If not specificly set, Retromock will use defaults equivalent to
```java
  @MockBehavior(durationDeviation = 1000, durationMillis = 500)
```

Retromock declaration
-------
#### `BodyFactory`

In order to provide a response body from a custom source create a BodyFactory implementation and set it to `bodyFactory` parametar in `@MockResponse` annotation.
###### Example
1. Create an implementation of `BodyFactory` that loads a stream using application class loader.
```java
class ResourceBodyFactory implements BodyFactory {

  @Override
  public InputStream create(final String input) throws IOException {
    return ResourceBodyFactory.class.getResourceAsStream(input);
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
2. Create a `Retromock` instance and add body factory. Note: Body factory class cannot be annonymous class because you need to reference it later on.
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .addBodyFactory(new ResourceBodyFactory())
  .build();
```
3. In the service set your body factory class in `@MockResponse` annotation.
```java
public interface Service {

  @Mock
  @MockResponse(body = "smith.json", bodyFactory = ResourceBodyFactory.class)
  @GET("/")
  Call<User> getUser();
}
```

If majority of your service method use a particular body factory you can set it as default.
If so, you wouldn't need to specify a `bodyFactory` parameter in `@MockResponse` annotation.
Also, in this case `BodyFactory` instance can be annonymous class.

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

#### `Behavior`
Implementation of this class provides a response delay in milliseconds.

If you want to set a custom default delay implement this class and set it as `defaultBehavior` in the builder.

If so, this behavior will be used on all service method that do not have `@MockBehavior` annotation.

If not set, Retromock uses default behavior that produces response delays randomly in uniform distribution between 500 and 1500 milliseconds.
###### Example
Remove response delay
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBehavior(() -> 0)
  .build();
```

#### `Executors`
You can set background and callback executors in `Retromock` builder.

By default, background executor is set to mock a call on background thread.
If you set a custom implementation keep in mind that response delay will block the thread.

Retromock by default uses callback executor from `Retrofit` instance.
If you want a custom one, feel free to set it using the builder.


###### Example
Set custom default behavior in the builder.
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBehavior(() -> 0)
  .build();
```

ProGuard
-------
The library does not require any ProGuard rules.

However, you might would need rules for Retrofit and its dependencies.

License
-------
```
Copyright 2019 Infinum

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Credits

Maintained and sponsored by [Infinum](http://www.infinum.co).

<a href='https://infinum.co'>
  <img src='https://infinum.co/infinum.png' href='https://infinum.co' width='264'>
</a>