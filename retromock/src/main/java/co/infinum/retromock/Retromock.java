package co.infinum.retromock;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Okio;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import co.infinum.retromock.meta.*;

/**
 * Retromock adapts {@link Retrofit} created Java interface using annotations on declared methods
 * to define if response should be mocked or not.
 * <p>
 * For example,
 * <pre><code>
 * Retromock retromock = new Retromock.Builder()
 *    .retrofit(retrofit)
 *    .build();
 *
 * MyApi api = retromock.create(MyApi.class);
 * Response&lt;User&gt; user = api.getUser().execute();
 * </code></pre>
 *
 * <p>
 * To enable mocking of some method it has to be annotated with
 * {@link Mock} annotation. If there is no
 * {@link Mock} annotation or the annotation has value set to {@code
 * false} method call would be delegated to retrofit's instance of the service.
 * <pre><code>
 * &#064;Mock
 * Call&lt;User&gt; getUser();
 * </code></pre>
 *
 * <p>
 * To return specific mock response method has to be annotated with
 * {@link MockResponse} annotation and specify response parameters in
 * the annotation. For example,
 * <pre><code>
 * &#064;Mock
 * &#064;MockResponse(code = 200, message = "OK", body = "{\"name\":\"John\",
 * \"surname\":\"Smith\"}",
 * headers = {
 *  &#064;MockHeader(name = "ContentType", value = "application/json"),
 *  &#064;MockHeader(name = "CustomHeader", value = "CustomValue")
 * }, bodyFactory = PassThroughBodyFactory.class)
 * Call&lt;User&gt; getUser();
 * </code></pre>
 * body parameter is closely related with bodyFactory parameter. Value of the body parameter will
 * be used to load body stream using bodyFactory instance. The implementation of creating body
 * stream is depending on instance of {@link BodyFactory}.
 *
 * <p>
 * Specify custom behavior on a method using {@link MockBehavior}
 * annotation. For example,
 * <pre><code>
 * &#064;Mock
 * &#064;MockBehavior(durationMillis = 500, durationDeviation = 100)
 * Call&lt;User&gt; getUser();
 * </code></pre>
 * would delay response between 400 and 600 milliseconds.
 */
public final class Retromock {

  static final class DisabledException extends Exception {}

  private final Retrofit retrofit;
  private final Map<Class<? extends BodyFactory>, BodyFactory> bodyFactories;
  private final Map<Method, RetromockMethod> methodCache;

  private final boolean eagerlyLoad;
  private final ExecutorService backgroundExecutor;
  private final Executor callbackExecutor;

  private final Behavior defaultBehavior;
  private final BodyFactory defaultBodyFactory;

  private Retromock(final Retrofit retrofit,
    final Map<Class<? extends BodyFactory>, BodyFactory> bodyFactories,
    final boolean eagerlyLoad,
    final ExecutorService backgroundExecutor,
    final Executor callbackExecutor,
    final Behavior defaultBehavior,
    final BodyFactory bodyFactory) {

    this.retrofit = retrofit;
    this.bodyFactories = bodyFactories;
    this.methodCache = new HashMap<>();
    this.eagerlyLoad = eagerlyLoad;
    this.backgroundExecutor = backgroundExecutor;
    this.callbackExecutor = callbackExecutor;
    this.defaultBehavior = defaultBehavior;
    this.defaultBodyFactory = bodyFactory;
  }

  public <T> T create(final Class<T> service) {
    return create(createDelegate(retrofit, service), service);
  }

  @SuppressWarnings({"unchecked", "WeakerAccess"})
  public <T> T create(final DelegateFactory<T> factory, Class<T> service) {
    if (eagerlyLoad) {
      loadService(service);
    }
    final T delegate = factory.create();

    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[] {service},
      new InvocationHandler() {
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws
          Throwable {
          if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
          }

          RetromockMethod mockMethod;
          try {
            mockMethod = findRetromockMethod(method);
          } catch (DisabledException e) {
            // Retromock is ignored on this method!
            return method.invoke(delegate, args);
          }

          final CallAdapter<?, T> callAdapter = (CallAdapter<?, T>) retrofit
            .callAdapter(method.getGenericReturnType(), method.getAnnotations());

          final ParamsProducer producer = mockMethod.producer();

          Call<?> mockedCall = Calls.defer(new Callable<Call<T>>() {
            @Override
            public Call<T> call() throws IOException {
              return Calls.response(createResponse(
                retrofit.<T>responseBodyConverter(
                  callAdapter.responseType(),
                  method.getAnnotations()
                ), producer.produce()
              ));
            }
          });

          return callAdapter.adapt(new RetromockCall(
            mockMethod.behavior(),
            backgroundExecutor,
            callbackExecutor,
            mockedCall
          ));
        }
      });
  }

  private RetromockMethod findRetromockMethod(Method method) throws DisabledException {
    RetromockMethod result = methodCache.get(method);
    if (result != null) {
      return result;
    }

    synchronized (methodCache) {
      result = methodCache.get(method);
      if (result == null) {
        result = RetromockMethod.parse(method, this);
        methodCache.put(method, result);
      }
    }

    return result;
  }

  BodyFactory bodyFactory(final Class<? extends BodyFactory> type) {
    if (type == BodyFactory.class) {
      return defaultBodyFactory;
    }
    BodyFactory factory = bodyFactories.get(type);
    if (factory != null) {
      return factory;
    }
    throw new IllegalStateException(
      "BodyFactory for type " + type.getName() + " does not exist.");
  }

  BodyFactory defaultBodyFactory() {
    return defaultBodyFactory;
  }

  Behavior defaultBehavior() {
    return defaultBehavior;
  }

  private static <T> DelegateFactory<T> createDelegate(
    final Retrofit retrofit, final Class<T> service) {

    return new DelegateFactory<T>() {
      @Override
      public T create() {
        return retrofit.create(service);
      }
    };
  }

  private void loadService(Class<?> service) {
    for (Method method : service.getDeclaredMethods()) {
      try {
        findRetromockMethod(method);
      } catch (DisabledException ignored) {
      }
    }
  }

  private static <T> Response<T> createResponse(
    Converter<ResponseBody, T> converter,
    ResponseParams params) throws IOException {

    RetromockBodyFactory factory = params.bodyFactory();

    ResponseBody responseBody = null;
    if (factory != null) {

      String contentType = params.contentType();
      MediaType mediaType = null;
      if (contentType != null) {
        mediaType = MediaType.parse(contentType);
      }

      responseBody = ResponseBody.create(
        mediaType,
        params.contentLength(),
        Okio.buffer(Okio.source(factory.createBody())));
    }

    okhttp3.Response rawResponse = new okhttp3.Response.Builder()
      .code(params.code())
      .message(params.message())
      .body(responseBody)
      .protocol(Protocol.HTTP_1_1)
      .headers(params.headers())
      .request(new Request.Builder().url("http://localhost").build())
      .build();

    assert rawResponse.body() != null;
    if (!rawResponse.isSuccessful()) {
      return Response.error(rawResponse.body(), rawResponse);
    } else {
      try {
        T body = null;
        if (rawResponse.code() != HttpURLConnection.HTTP_NO_CONTENT
          && rawResponse.code() != HttpURLConnection.HTTP_RESET) {
          body = converter.convert(rawResponse.body());
        }
        return Response.success(body, rawResponse);
      } catch (IOException e) {
        throw new RuntimeException("Error while converting mocked response!", e);
      }
    }
  }

  private static class SyncExecutor implements Executor {

    @Override
    public void execute(final Runnable command) {
      command.run();
    }
  }

  /**
   * Build a new {@link Retromock} instance.
   * Calling {@link #retrofit(Retrofit)} is required before calling {@link #build()}. All other
   * methods are optional.
   */
  public static class Builder {

    private Retrofit retrofit;
    private Map<Class<? extends BodyFactory>, BodyFactory> bodyFactories = new HashMap<>();
    private boolean loadEagerly;
    private ExecutorService backgroundExecutor;
    private Executor callbackExecutor;
    private Behavior defaultBehavior;
    private BodyFactory defaultBodyFactory;

    /**
     * The retrofit instance used to adapt calls and parse responses.
     * This instance is used to provide callback executor if not specified in the {@link Builder}.
     *
     * @param retrofit Retrofit instance.
     * @return this {@link Builder}.
     */
    public Builder retrofit(final Retrofit retrofit) {
      this.retrofit = retrofit;
      return this;
    }

    /**
     * Add new {@link BodyFactory} instance for creating body's {@link java.io.InputStream} from
     * specified input.
     *
     * @param bodyFactory Body stream creator.
     * @return this {@link Builder}.
     */
    public Builder addBodyFactory(final BodyFactory bodyFactory) {
      Preconditions.checkNotNull(bodyFactory, "Body factory is null.");
      this.bodyFactories.put(bodyFactory.getClass(), bodyFactory);
      return this;
    }

    /**
     * Define a custom {@link BodyFactory} that is used only if
     * {@link co.infinum.retromock.meta.MockResponse} bodyFactory is not specified explicitly.
     * If not set, {@link PassThroughBodyFactory} is used as default.
     *
     * @param defaultBodyFactory Body stream creator.
     * @return this {@link Builder}.
     */
    public Builder defaultBodyFactory(final BodyFactory defaultBodyFactory) {
      this.defaultBodyFactory = defaultBodyFactory;
      return this;
    }

    /**
     * When calling {@link #create} on the resulting {@link Retrofit} instance, eagerly validate
     * and load the configuration of all methods in the supplied interface.
     *
     * @param loadEagerly true to eagerly validate and load the configuration.
     * @return this {@link Builder}.
     */
    public Builder loadEagerly(final boolean loadEagerly) {
      this.loadEagerly = loadEagerly;
      return this;
    }

    /**
     * Executor used to execute a call.
     * Defaults to Executors.newSingleThreadExecutor if not specified explicitly.
     *
     * @param backgroundExecutor Executor used to execute a call.
     * @return this {@link Builder}.
     */
    //TODO: not sure if this should be publicly configurable
    public Builder backgroundExecutor(final ExecutorService backgroundExecutor) {
      this.backgroundExecutor = backgroundExecutor;
      return this;
    }

    /**
     * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
     * your service method.
     *
     * @param callbackExecutor Executor to invoke callback on.
     * @return this {@link Builder}.
     * @see Retrofit.Builder callbackExecutor
     */
    public Builder callbackExecutor(final Executor callbackExecutor) {
      this.callbackExecutor = callbackExecutor;
      return this;
    }

    /**
     * Specify custom behavior which will be used whenever
     * {@link co.infinum.retromock.meta.MockBehavior} annotation is not present on the method.
     *
     * @param defaultBehavior Behavior instance used as default one.
     * @return this {@link Builder}.
     */
    public Builder defaultBehavior(@Nullable final Behavior defaultBehavior) {
      this.defaultBehavior = defaultBehavior;
      return this;
    }

    /**
     * Create the {@link Retromock} instance using the configured values.
     *
     * If callbackExecutor is not specified, the one from retrofit will be used instead if exist.
     * If not {@link SyncExecutor} will be used.
     *
     * If default {@link Behavior} is not specified, {@link DefaultBehavior} will be used with
     * default parameters to deviate response fetch time between 0.5 and 1.5 seconds.
     *
     * If default {@link BodyFactory} is not specified, {@link PassThroughBodyFactory} will be
     * used instead to create body from the text entered in
     * {@link co.infinum.retromock.meta.MockResponse}'s body.
     *
     * @return create new {@link Retromock} instance using values configured using this builder.
     */
    public Retromock build() {
      Preconditions.checkNotNull(retrofit, "Retrofit is null.");

      Map<Class<? extends BodyFactory>, BodyFactory> bodyFactories =
        new HashMap<>((this.bodyFactories));
      bodyFactories.put(PassThroughBodyFactory.class, new PassThroughBodyFactory());

      ExecutorService backgroundExecutor = this.backgroundExecutor;
      if (backgroundExecutor == null) {
        backgroundExecutor = Executors.newSingleThreadExecutor(new DefaultThreadFactory());
      }

      Executor callbackExecutor = this.callbackExecutor;
      if (callbackExecutor == null) {
        callbackExecutor = retrofit.callbackExecutor();
      }
      if (callbackExecutor == null) {
        callbackExecutor = new SyncExecutor();
      }

      Behavior behavior = this.defaultBehavior;
      if (behavior == null) {
        behavior = DefaultBehavior.INSTANCE;
      }

      BodyFactory bodyFactory = this.defaultBodyFactory;
      if (bodyFactory == null) {
        bodyFactory = new PassThroughBodyFactory();
      }

      return new Retromock(
        retrofit,
        Collections.unmodifiableMap(bodyFactories),
        loadEagerly,
        backgroundExecutor,
        callbackExecutor,
        behavior,
        bodyFactory
      );
    }
  }
}
