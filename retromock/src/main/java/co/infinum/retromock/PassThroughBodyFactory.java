package co.infinum.retromock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import co.infinum.retromock.meta.MockResponse;
import okio.Okio;

/**
 * Implementation of {@link BodyFactory} that creates body from given input.
 * Use when pass full response body in {@link MockResponse} annotation.
 */
public final class PassThroughBodyFactory implements BodyFactory {

  @Override
  public InputStream create(@Nonnull final String input) {
    return Okio.buffer(Okio.source(
      new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
    ).inputStream();
  }
}
