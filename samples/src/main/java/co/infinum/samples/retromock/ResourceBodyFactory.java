package co.infinum.samples.retromock;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import co.infinum.retromock.BodyFactory;

final class ResourceBodyFactory implements BodyFactory {

  @Override
  public InputStream create(final String input) throws IOException {
    // this will throw if input is empty string, regular class loader opens a stream to directory
    return new FileInputStream(
      ResourceBodyFactory.class.getClassLoader().getResource(input).getFile()
    );
  }
}
