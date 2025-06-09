package co.infinum.retromock;

import retrofit2.Call;
import retrofit2.Response;
import kotlin.coroutines.Continuation;

import java.lang.reflect.Type;

class SuspendCallWrapper<T> implements CallWrapper {

    private Type returnType;
    private Type actualType;
    private boolean continuationWantsResponse;

    SuspendCallWrapper(final Type returnType, final Type rawReturnType, final boolean continuationWantsResponse) {
        this.returnType = returnType;
        this.continuationWantsResponse = continuationWantsResponse;
        this.actualType = rawReturnType;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Type getActualType() {
        return actualType;
    }

    @SuppressWarnings("unchecked")
    private Object handleContinuationWithResponse(final Object[] args, final Object call) {
        Continuation<Response<T>> continuation = (Continuation<Response<T>>) args[args.length - 1];

        // See SuspendForBody for explanation about this try/catch.
        try {
            return KotlinExtensions.awaitResponse((Call<T>) call, continuation);
        } catch (Exception e) {
            return KotlinExtensions.suspendAndThrow(e, continuation);
        }
    }

    @SuppressWarnings("unchecked")
    private Object handleContinuation(final Object[] args, final Object call) {
        Continuation<T> continuation = (Continuation<T>) args[args.length - 1];
        try {
            return KotlinExtensions.await((Call<T>) call, continuation);
        } catch (Exception e) {
            return KotlinExtensions.suspendAndThrow(e, continuation);
        }
    }

    @Override
    public Object wrap(final Object call, final Object[] args) {
        if (continuationWantsResponse) {
            return handleContinuationWithResponse(args, call);
        } else {
            return handleContinuation(args, call);
        }
    }
}

