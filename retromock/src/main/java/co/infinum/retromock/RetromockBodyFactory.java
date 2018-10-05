package co.infinum.retromock;

import java.io.IOException;
import java.io.InputStream;

final class RetromockBodyFactory {

  private final BodyFactory bodyFactory;

  private final String input;

  RetromockBodyFactory(final BodyFactory bodyFactory, final String input) {
    this.bodyFactory = bodyFactory;
    this.input = input;
  }

  InputStream createBody() throws IOException {
    return bodyFactory.create(input);
  }
}
