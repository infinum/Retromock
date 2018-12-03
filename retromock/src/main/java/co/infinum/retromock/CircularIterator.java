package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

final class CircularIterator<T> implements ResponseIterator<T> {

  private final T[] responses;

  private AtomicInteger index = new AtomicInteger();

  CircularIterator(final T[] responses) {
    Preconditions.checkNotEmpty(responses, "Responses should contain at least one response.");
    this.responses = responses;
  }

  @Override
  public T next() {
    return responses[index.getAndIncrement() % responses.length];
  }
}
