package co.infinum.retromock;

import java.util.concurrent.ThreadLocalRandom;

class ThreadLocalRandomProvider implements RandomProvider {

  @Override
  public long nextLong(final long bound) {
    return ThreadLocalRandom.current().nextLong(bound);
  }
}
