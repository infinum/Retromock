package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

final class SequentialIterator<T> implements ResponseIterator<T> {

  private final T[] responses;

  private AtomicInteger index = new AtomicInteger();

  SequentialIterator(final T[] responses) {
    Preconditions.checkNotEmpty(responses, "Responses should contain at least one response.");
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
