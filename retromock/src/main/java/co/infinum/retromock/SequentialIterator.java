package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

import co.infinum.retromock.meta.MockResponse;

final class SequentialIterator implements ResponseIterator {

  private final MockResponse[] responses;

  private AtomicInteger index = new AtomicInteger();

  SequentialIterator(final MockResponse[] responses) {
    this.responses = responses;
  }

  @Override
  public MockResponse next() {
    int index = this.index.getAndIncrement();
    if (index >= responses.length) {
      return responses[responses.length - 1];
    } else {
      return responses[index];
    }
  }
}
