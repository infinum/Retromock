/**
 * Retromock adapts {@link retrofit2.Retrofit} created Java interface using annotations on
 * declared methods to define if response should be mocked or not.
 * <p>
 * For example,
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
 */

@ParametersAreNonnullByDefault
package co.infinum.retromock;

import javax.annotation.ParametersAreNonnullByDefault;