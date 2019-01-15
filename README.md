# Retromock

Adapts Java interface created by [Retrofit][retrofit] using annotations on declared methods to define response mocks.

Quick guide
-------

#### Add dependency
```gradle
implementation 'co.infinum:retromock:0.1.0'
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

##### Load responses from streams
If you would like to load response from a stream set a default body factory that takes a body parameter(`response.json`) and loads a response stream for it.
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(input -> openStream(input))
  .build();
```

```java
public interface Service {

  @Mock
  @MockResponse(body = "response.json")
  @GET("/endpoint")
  Call<User> getUser();
}
```

##### Load responses from Android assets
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(context.getAssets()::open)
  .build();
```

```java
public interface Service {

  @Mock
  @MockResponse(body = "retromock/response.json")
  @GET("/endpoint")
  Call<User> getUser();
}
```

Save a response body content in file named `retromock/response.json`.

If you use `Retromock` only in some variants you can exclude files with mock responses from final .apk with configuration similar to:
```groovy
applicationVariants.all { variant ->
  if (variant.buildType.name.contains('production')) {
    variant.mergeAssets.doLast {
      delete(fileTree(dir: variant.mergeAssets.outputDir, includes: ['**/retromock/*']))
    }
  }
}
```

#### For more information please see [the full specification][specification].
-------



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


 [retrofit]: https://square.github.io/retrofit/
 [specification]: SPECIFICATION.md