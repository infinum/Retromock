package co.infinum.retromock;

/**
 * Simulates network behavior for local implementation of a call.
 * Produce a delay in milliseconds.
 */
public interface Behavior {

  /**
   * Produces each time new delay in milliseconds.
   *
   * @return Delay in milliseconds.
   */
  long delayMillis();
}
