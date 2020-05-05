package co.infinum.retromock;

import kotlin.coroutines.Continuation;
import retrofit2.Call;
import retrofit2.Response;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract class CallWrapper {

  abstract Object wrap(Object call);

  abstract Type getReturnType();

  /**
   * Checks if method is suspend kotlin fun and in that case wraps its return type into Retrofit's {@code Call}.
   * Also, method handles special case when return type is wrapped into Retrofit's {@code Response}.
   */
  static <T> CallWrapper create(Method method, final Object[] args) {
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

class NoOpCallWrapper extends CallWrapper {

  private Type returnType;

  public NoOpCallWrapper(Type returnType) {
    this.returnType = returnType;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public Object wrap(Object call) {
    return call;
  }
}

class SuspendCallWrapper<T> extends CallWrapper {

  private Type returnType;
  private boolean continuationWantsResponse;
  private Object[] args;

  public SuspendCallWrapper(Type returnType, boolean continuationWantsResponse, Object[] args) {
    this.returnType = returnType;
    this.continuationWantsResponse = continuationWantsResponse;
    this.args = args;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @SuppressWarnings("unchecked")
  private Object handleContinuationWithResponse(Object[] args, Object call) {
    //noinspection unchecked Checked by reflection inside RequestFactory.
    Continuation<Response<T>> continuation = (Continuation<Response<T>>) args[args.length - 1];

    // See SuspendForBody for explanation about this try/catch.
    try {
      return KotlinExtensions.awaitResponse((Call<T>) call, continuation);
    } catch (Exception e) {
      return KotlinExtensions.suspendAndThrow(e, continuation);
    }
  }

  @SuppressWarnings("unchecked")
  private Object handleContinuation(Object[] args, Object call) {
    //noinspection unchecked Checked by reflection inside RequestFactory.
    Continuation<T> continuation = (Continuation<T>) args[args.length - 1];
    try {
      return KotlinExtensions.await((Call<T>) call, continuation);
    } catch (Exception e) {
      return KotlinExtensions.suspendAndThrow(e, continuation);
    }
  }

  @Override
  public Object wrap(Object call) {
    if (continuationWantsResponse) {
      return handleContinuationWithResponse(args, call);
    } else {
      return handleContinuation(args, call);
    }
  }
}
