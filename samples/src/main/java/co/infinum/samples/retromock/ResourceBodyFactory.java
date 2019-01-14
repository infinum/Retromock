package co.infinum.samples.retromock;

import java.io.IOException;
import java.io.InputStream;

import co.infinum.retromock.BodyFactory;

class ResourceBodyFactory implements BodyFactory {

  @Override
  public InputStream create(final String input) throws IOException {
    return ResourceBodyFactory.class.getResourceAsStream(input);
  }
}
