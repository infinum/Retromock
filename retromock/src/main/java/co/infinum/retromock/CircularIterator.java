package co.infinum.retromock;

import java.util.concurrent.atomic.AtomicInteger;

import co.infinum.retromock.meta.MockResponse;

final class CircularIterator implements ResponseIterator {

  private final MockResponse[] responses;

  private AtomicInteger index = new AtomicInteger();

  CircularIterator(final MockResponse[] responses) {
    this.responses = responses;
  }

  @Override
  public MockResponse next() {
    return responses[index.getAndIncrement() % responses.length];
  }
}
