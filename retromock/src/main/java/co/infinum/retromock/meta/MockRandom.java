package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.infinum.retromock.*;

/**
 * Use this to specify resolution strategy when applying multiple responses. If service method
 * has multiple {@link MockResponse} annotations and if service method has this annotation
 * provided {@link Retromock} would pick one random response each time.
 * Note: this annotation is cannot coexist with {@link MockCircular} or {@link MockSequential}
 * because they specify different behavior for the same purpose.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockRandom {
}
