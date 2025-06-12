package co.infinum.retromock;

final class NoResponseProducer implements ParamsProducer {

    /**
     * The retromock instance used for configuration.
     */
  private final Retromock retromock;

    /**
     * The default response parameters to use as a base.
     */
  private final ResponseParams defaults;

  NoResponseProducer(final Retromock retromock, final ResponseParams defaults) {
    this.retromock = retromock;
    this.defaults = defaults;
  }

  @Override
  public ResponseParams produce(final Object[] args) {
    ResponseParams.Builder builder = defaults.newBuilder();
    return builder.bodyFactory(new RetromockBodyFactory(
      retromock.bodyFactory(PassThroughBodyFactory.class), "")).build();
  }
}
