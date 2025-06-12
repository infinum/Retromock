package co.infinum.retromock;

import java.io.IOException;
import java.io.InputStream;

final class RetromockBodyFactory {

    /**
     * The underlying body factory for creating input streams.
     */
  private final BodyFactory bodyFactory;

    /**
     * The input string to be converted to a body.
     */
  private final String input;

  RetromockBodyFactory(final BodyFactory bodyFactory, final String input) {
    this.bodyFactory = bodyFactory;
    this.input = input;
  }

  InputStream createBody() throws IOException {
    return bodyFactory.create(input);
  }
}
