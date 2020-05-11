package co.infinum.retromock;

import kotlin.coroutines.Continuation;
import retrofit2.Call;
import retrofit2.Response;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class CallWrapperFactory {

  /**
   * Checks if method is suspend kotlin fun and in that case wraps its return type into Retrofit's {@code Call}.
   * Also, method handles special case when return type is wrapped into Retrofit's {@code Response}.
   */
  static <T> CallWrapper create(final Method method, final Object[] args) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length > 0) {
      Type lastParameterType = parameterTypes[parameterTypes.length - 1];
      if (Utils.getRawType(lastParameterType) == Continuation.class) {
        Type actualType = Utils.getParameterLowerBound(0, (ParameterizedType) lastParameterType);
        Type returnType = new Utils.ParameterizedTypeImpl(null, Call.class, actualType);
        boolean continuationWantsResponse = false;

        if (Utils.getRawType(actualType) == Response.class && actualType instanceof ParameterizedType) {
          // Unwrap the actual body type from Response<T>.
          actualType = Utils.getParameterUpperBound(0, (ParameterizedType) actualType);
          returnType = new Utils.ParameterizedTypeImpl(null, Call.class, actualType);
          continuationWantsResponse = true;
        }

        return new SuspendCallWrapper<T>(returnType, continuationWantsResponse, args);
      }
    }
    return new NoOpCallWrapper(method.getGenericReturnType());
  }
}