package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

final class SequentialIterator<T> implements ResponseIterator<T> {

  /**
   * Array of responses to iterate through sequentially.
   */
  private final T[] responses;

  /**
   * Index tracker for the current position in the responses array.
   */
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
