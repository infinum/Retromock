package co.infinum.retromock;

/**
 * Creates an instance of service delegate.
 *
 * @param <T> Service class.
 */
public interface DelegateFactory<T> {

  /**
   * Create new instance of service.
   *
   * @return Service.
   */
  T create();
}
