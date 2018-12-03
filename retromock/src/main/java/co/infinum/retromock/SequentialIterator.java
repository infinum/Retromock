package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

final class SequentialIterator<T> implements ResponseIterator<T> {

  private final T[] responses;

  private AtomicInteger index = new AtomicInteger();

  SequentialIterator(final T[] responses) {
    this.responses = responses;
  }

  @Override
  public T next() {
    int currentIndex = this.index.getAndIncrement();
    if (currentIndex >= responses.length) {
      return responses[responses.length - 1];
    } else {
      return responses[currentIndex];
    }
  }
}
