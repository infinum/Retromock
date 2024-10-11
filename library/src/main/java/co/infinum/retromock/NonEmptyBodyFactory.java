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
 * returns <code>true</code>.
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
public final class NonEmptyBodyFactory implements BodyFactory {

  private final BodyFactory bodyFactory;

  /**
   * Creates a new instance of {@link BodyFactory} that wraps provided one.
   * In case of not empty input it calls delegates the call to wrapped instance.
   * Otherwise, it creates an empty body stream.
   * Input is considered to be empty if
   * <pre><code>
   *   input.trim().isEmpty()
   * </code></pre>
   * returns <code>true</code>
   *
   * @param bodyFactory instance to delegate a call to if body input is not empty
   */
  public NonEmptyBodyFactory(final BodyFactory bodyFactory) {
    Preconditions.checkNotNull(bodyFactory, "Body factory is null.");
    this.bodyFactory = bodyFactory;
  }

  @Override
  public InputStream create(@Nonnull final String input) throws IOException {
    Preconditions.checkNotNull(input, "Input is null.");
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
