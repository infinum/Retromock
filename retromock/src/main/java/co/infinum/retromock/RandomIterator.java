package co.infinum.retromock;

import java.util.concurrent.ThreadLocalRandom;

final class RandomIterator<T> implements ResponseIterator<T> {

  private final T[] responses;

  RandomIterator(final T[] responses) {
    this.responses = responses;
  }

  @Override
  public T next() {
    return responses[ThreadLocalRandom.current().nextInt(responses.length)];
  }
}
