# Retromock
[![Build Status](https://app.bitrise.io/app/7b832efc5bb97051/status.svg?token=S3Efgo8YEz6s8tFv2ocKzA&branch=master)](https://app.bitrise.io/app/7b832efc5bb97051)
[ ![Download](https://img.shields.io/maven-central/v/co.infinum/retromock) ](https://search.maven.org/artifact/co.infinum/retromock)

<img src='./logo.svg' width='264'/>

Adapts Java interface created by [Retrofit][retrofit] using annotations on declared methods to define response mocks.

Quick guide
-------

#### Add dependency
```gradle
implementation 'co.infinum:retromock:1.1.1'
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
If you would like to load response from a stream set a default body factory that loads a response stream by a body parameter(`response.json`) in annotation.
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(...)
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

Note: if you set custom default body factory and do not declare a `bodyFactory` parameter in `@MockResponse` annotation your body factory will be called with value of `body` parameter.
That also applies if you don't specificaly set a `body` - in that case `body` is empty by default.
If you wouldn't like to handle the case of empty `body` wrap your default body factory into `NonEmptyBodyFactory` class as follows:
```java
Retromock retromock = new Retromock.Builder()
  .retrofit(retrofit)
  .defaultBodyFactory(new NonEmptyBodyFactory(...))
  .build();
```

#### For more information please see [the full specification][specification].
-------



ProGuard
-------
The library does not require any ProGuard rules.

However, you might need rules for Retrofit and its dependencies.

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

<p align="center">
  <a href='https://infinum.com'>
    <picture>
        <source srcset="https://assets.infinum.com/brand/logo/static/white.svg" media="(prefers-color-scheme: dark)">
        <img src="https://assets.infinum.com/brand/logo/static/default.svg">
    </picture>
  </a>
</p>

 [retrofit]: https://square.github.io/retrofit/
 [specification]: SPECIFICATION.md
