package co.infinum.retromock.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to provide a class that has ability to provide a
 * {@link co.infinum.retromock.Response}.
 *
 * Class should have exactly one method of same signature as service method, except the return
 * type - it should be of type {@link co.infinum.retromock.Response}.
 * Also, the method should be annotated with @{@link ProvidesMock} annotation.
 *
 * If service method is annotated with this annotation, {@link co.infinum.retromock.Retromock}
 * will use it for each mock call to provide a mock response.
 * Note: either this or @{@link MockResponse} annotation(s) should be used on a single service
 * method.
 *
 * Example:
 *  1. in service annotate a method with this annotation
 *  <pre><code>
 *    &#064;Mock
 *    &#064;MockResponseProvider(MockProvider.class)
 *    &#064;GET("api")
 *    Call&lt;User&gt; getUser(&#064;Query("id") String id);
 *  </code></pre>
 *  2. Create a MockProvider class with following content:
 *  <pre><code>
 *    &#064;ProvidesMock
 *    Response getUser(&#064;Query("id") String id) {
 *      // return a mock response id...
 *    }
 *  </code></pre>
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MockResponseProvider {

  /**
   * Class type of a class that has a providing method.
   *
   * For example, if class that provides responses is <code>MockProvider</code>, set this
   * value to <code>MockProvider.class</code>
   * @return Class type.
   */
  Class<?> value();

}
