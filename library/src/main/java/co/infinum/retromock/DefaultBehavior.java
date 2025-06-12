package co.infinum.retromock;

/**
 * Default implementation of {@link Behavior}. Produces a random delay with specified deviation.
 */
public class DefaultBehavior implements Behavior {

    /**
     * Default instance with 1000ms duration and 500ms deviation.
     */
  static final DefaultBehavior INSTANCE = new DefaultBehavior(1000L, 500);

    /**
     * The base delay duration in milliseconds.
     */
  private final long durationMillis;

    /**
     * The deviation range for randomizing the delay.
     */
  private final int durationDeviation;

    /**
     * Provider for random number generation.
     */
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
   * This instance would provide a delay in range [duration - deviation, duration + deviation)
   *
   * @param durationMillis    Delay duration in milliseconds.
   * @param durationDeviation Delay deviation in milliseconds.
   * @param randomProvider    Provides random generator
   */
  DefaultBehavior(final long durationMillis, final int durationDeviation,
    final RandomProvider randomProvider) {
    if (durationDeviation < 0) {
      throw new IllegalArgumentException("Behavior deviation must be positive or zero.");
    }
    this.durationMillis = durationMillis;
    this.durationDeviation = durationDeviation;
    this.randomProvider = randomProvider;
  }

  @Override
  public final long delayMillis() {
    if (durationDeviation == 0) {
      return durationMillis;
    } else {
      return durationMillis
        + randomProvider.nextLong(durationDeviation * 2L)
        - durationDeviation;
    }
  }
}
