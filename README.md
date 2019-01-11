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
###### Example in Java
```java
  @Mock
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Smith\"}")
  @MockResponse(body = "{\"name\":\"John\", \"surname\":\"Doe\"}")
  @GET("/")
  Call<User> getUser();
```
###### Example in Kotlin
```kotlin
  @Mock
  @MockResponses(
    MockResponse(body = "Body example."),
    MockResponse(body = "Body example 2."),
    MockResponse(body = "Body example 3.")
  )
  @GET("/")
  fun getResponseBody(): Call<ResponseBody>
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