package co.infinum.retromock;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import co.infinum.retromock.meta.MockResponse;

/**
 * Creates new {@link InputStream} instance for the given body parameter in
 * {@link MockResponse} annotation provided on a method. Based on the
 * input this class loads actual response stream.
 */
public interface BodyFactory {

  /**
   * Creates new instance of {@link InputStream} for the given input.
   *
   * @param input String provided in {@link MockResponse} annotation body.
   * @return New instance of a stream.
   * @throws IOException In case of any IO error while loading a stream.
   */
  InputStream create(@Nonnull String input) throws IOException;
}
