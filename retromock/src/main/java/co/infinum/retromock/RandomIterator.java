package co.infinum.retromock;

final class RandomIterator<T> implements ResponseIterator<T> {

  private final T[] responses;
  private final RandomProvider randomProvider;

  RandomIterator(final T[] responses) {
    this(responses, new ThreadLocalRandomProvider());
  }

  RandomIterator(final T[] responses, final RandomProvider randomProvider) {
    Preconditions.checkNotNullOrEmpty(responses, "");
    this.responses = responses;
    this.randomProvider = randomProvider;
  }

  @Override
  public T next() {
    return responses[randomProvider.nextInt(responses.length)];
  }
}
