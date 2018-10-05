package co.infinum.retromock;

import java.util.concurrent.ThreadLocalRandom;

import co.infinum.retromock.meta.MockResponse;

final class RandomIterator implements ResponseIterator {

  private final MockResponse[] responses;

  RandomIterator(final MockResponse[] responses) {
    this.responses = responses;
  }

  @Override
  public MockResponse next() {
    return responses[ThreadLocalRandom.current().nextInt(responses.length)];
  }
}
