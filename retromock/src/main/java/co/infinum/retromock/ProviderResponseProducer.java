package co.infinum.retromock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import co.infinum.retromock.meta.MockResponseProvider;
import co.infinum.retromock.meta.ProvidesMock;
import okhttp3.Headers;

final class ProviderResponseProducer implements ParamsProducer {

  private final Object provider;
  private final Method providerMethod;
  private final Retromock retromock;

  ProviderResponseProducer(
    final MockResponseProvider annotation,
    final Method serviceMethod,
    final Retromock retromock
  ) throws IllegalAccessException, InstantiationException, NoSuchMethodException,
    InvocationTargetException {
    this.retromock = retromock;
    provider = annotation.value().getConstructor().newInstance();
    providerMethod = findProducerMethod(annotation.value(), serviceMethod);
  }

  @Override
  public ResponseParams produce(final Object[] args) {
    try {
      Response response = (Response) providerMethod.invoke(provider, args);
      return new ResponseParams.Builder()
        .code(response.code())
        .message(response.message())
        .headers(Headers.of())
        .bodyFactory(new RetromockBodyFactory(
          retromock.bodyFactory(response.bodyFactoryClass()),
          response.body()
        ))
        .build();
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("ResponseProducer " + provider.getClass() + " threw!", e);
    }
  }

  private static Method findProducerMethod(
    final Class<?> providerClass,
    final Method serviceMethod) {

    Method providerMethod = null;
    for (Class<?> c = providerClass; c != Object.class; c = c.getSuperclass()) {
      for (Method method : c.getDeclaredMethods()) {
        if (method.isAnnotationPresent(ProvidesMock.class)
          && Response.class.equals(method.getReturnType())
          && Arrays.equals(method.getParameterTypes(), serviceMethod.getParameterTypes())) {
          if (providerMethod == null) {
            providerMethod = method;
          } else {
            throw new IllegalArgumentException(
              "More than one annotated mock provider methods with same signature found in the "
                + providerClass + " class. Found " + providerMethod.getDeclaringClass() + "."
                + providerMethod.getName() + " and " + method.getDeclaringClass() + "."
                + method.getName()
            );
          }
        }
      }
    }

    if (providerMethod == null) {
      throw new IllegalArgumentException("Couldn't find a single method annotated with mock "
        + "provider method in provider class: " + providerClass.getName() + ". Exactly one method"
        + " with following properties should be in the class:\n"
        + " * method must be annotated with @ProvidesMock\n"
        + " * return type has to be Response class\n"
        + " * all method arguments should match service method. Service method has following "
        + "arguments:\n"
        + "   " + Arrays.toString(serviceMethod.getParameterTypes()) + "."
      );
    }
    return providerMethod;
  }
}
