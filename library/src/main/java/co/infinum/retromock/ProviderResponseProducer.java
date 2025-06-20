package co.infinum.retromock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import co.infinum.retromock.meta.ProvidesMock;

final class ProviderResponseProducer implements ParamsProducer {

    /**
     * The instance of the provider class.
     */
    private final Object provider;

    /**
     * The method on the provider that produces mock responses.
     */
    private final Method providerMethod;

    /**
     * The retromock instance for configuration access.
     */
    private final Retromock retromock;

    ProviderResponseProducer(
            final Class<?> providerClass,
            final Method serviceMethod,
            final Retromock retromock
    ) {
        this.retromock = retromock;
        this.providerMethod = findProducerMethod(providerClass, serviceMethod, retromock);
        this.provider = createProvider(providerClass);
    }

    @Override
    public ResponseParams produce(final Object[] args) {
        Response response;
        try {
            response = (Response) providerMethod.invoke(provider, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("This shouldn't happen. Guarding against it in find method.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Method " + providerMethod.getDeclaringClass() + "."
                    + providerMethod.getName() + " threw an exception while executing.", e.getCause());
        }
        return new ResponseParams.Builder()
                .code(response.code())
                .message(response.message())
                .headers(response.headers())
                .bodyFactory(new RetromockBodyFactory(
                        retromock.bodyFactory(response.bodyFactoryClass()),
                        response.body()
                ))
                .build();
    }

    private static Method findProducerMethod(
            final Class<?> providerClass,
            final Method serviceMethod,
            final Retromock retromock) {

        Method providerMethod = null;
        for (Class<?> c = providerClass; c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                CallWrapper callWrapper = retromock.findCallWrapper(method);
                if (method.isAnnotationPresent(ProvidesMock.class)
                        && Response.class.equals(callWrapper.getActualType())
                        && isMethodApplicable(method, serviceMethod)) {
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
            throw new IllegalArgumentException("Couldn't find a single method annotated with "
                    + "@ProvidesMock in provider class: " + providerClass.getName() + ". Exactly one method"
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

    private static boolean isMethodApplicable(
            final Method candidate,
            final Method serviceMethod) {

        int modifiers = candidate.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers)) {
            throw new RuntimeException("Method annotated with @ProvidesMock should be public and concrete.");
        }

        return ContinuationUtilsKt
                .getActualParameterTypes(candidate)
                .equals(ContinuationUtilsKt.getActualParameterTypes(serviceMethod));
    }

    private static Object createProvider(final Class<?> providerClass) {
        try {
            return providerClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(
                    providerClass.getName() + " shouldn't be an abstract class.\n"
                            + "Retromock needs to instantiate the class. Please provide a concrete class instead.",
                    e
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    providerClass.getName() + " should have public default constructor.\n"
                            + "Retromock uses default constructor to create an instance of the class.",
                    e
            );
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    providerClass.getName() + " threw an exception during initialization.",
                    e.getCause()
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    providerClass.getName() + " has no default constructor.\n"
                            + "Retromock uses default constructor to create an instance of the class.",
                    e
            );
        }
    }

    Object provider() {
        return provider;
    }

    Method providerMethod() {
        return providerMethod;
    }
}
