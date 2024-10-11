package co.infinum.retromock;

final class NoResponseProducer implements ParamsProducer {

  private final Retromock retromock;
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
