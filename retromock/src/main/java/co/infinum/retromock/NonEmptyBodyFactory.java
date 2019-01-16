package co.infinum.retromock;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * Wraps a body factory instance so it is not called for empty input.
 * Input is considered to be empty if it
 * <pre><code>
 *   input.trim().isEmpty()
 * </code></pre>
 * returns true.
 * This class is used to skip default body factory in case of empty input.
 * For example, following example would still call your default body factory with empty input.
 * <pre><code>
 *   &#064;Mock
 *   &#064;MockResponse(code = 400)
 *   &#064;GET("/endpoint")
 *   Call&lt;User&gt; getUser();
 * </code></pre>
 * Wrap it to this class to handle that case with no response.
 */
public class NonEmptyBodyFactory implements BodyFactory {

  private final BodyFactory bodyFactory;

  public NonEmptyBodyFactory(final BodyFactory bodyFactory) {
    this.bodyFactory = bodyFactory;
  }

  @Override
  public InputStream create(@Nonnull final String input) throws IOException {
    if (input.trim().isEmpty()) {
      return new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };
    } else {
      return bodyFactory.create(input);
    }
  }
}
