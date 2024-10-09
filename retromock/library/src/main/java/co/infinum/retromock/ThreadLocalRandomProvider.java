package co.infinum.retromock;

import java.util.concurrent.ThreadLocalRandom;

final class ThreadLocalRandomProvider implements RandomProvider {

  @Override
  public long nextLong(final long bound) {
    return ThreadLocalRandom.current().nextLong(bound);
  }

  @Override
  public int nextInt(final int bound) {
    return ThreadLocalRandom.current().nextInt(bound);
  }
}
