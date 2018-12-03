package co.infinum.retromock;

/**
 * Default implementation of {@link Behavior}. Produces a random delay with specified deviation.
 */
public class DefaultBehavior implements Behavior {

  static final DefaultBehavior INSTANCE = new DefaultBehavior(1000, 500);

  private final long durationMillis;
  private final int durationDeviation;

  private final RandomProvider randomProvider;

  /**
   * Create new instance using delay duration and delay deviation.
   * This instance would provide a dealy in range [duration - deviation, duration + deviation)
   *
   * @param durationMillis    Delay duration in milliseconds.
   * @param durationDeviation Delay deviation in milliseconds.
   */
  DefaultBehavior(final long durationMillis, final int durationDeviation) {
    this(durationMillis, durationDeviation, new ThreadLocalRandomProvider());
  }

  /**
   * Create new instance using delay duration and delay deviation.
   * This instance would provide a dealy in range [duration - deviation, duration + deviation)
   *
   * @param durationMillis    Delay duration in milliseconds.
   * @param durationDeviation Delay deviation in milliseconds.
   * @param randomProvider    Provides random generator
   */
  DefaultBehavior(final long durationMillis, final int durationDeviation,
    RandomProvider randomProvider) {
    this.durationMillis = durationMillis;
    this.durationDeviation = durationDeviation;
    this.randomProvider = randomProvider;
  }

  @Override
  public long delayMillis() {
    return durationMillis
      + randomProvider.nextLong(durationDeviation * 2)
      - durationDeviation;
  }
}
