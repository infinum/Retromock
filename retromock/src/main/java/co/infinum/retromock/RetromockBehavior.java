package co.infinum.retromock;

import co.infinum.retromock.meta.MockBehavior;

final class RetromockBehavior extends DefaultBehavior {

  RetromockBehavior(final MockBehavior data) {
    super(data.durationMillis(), data.durationDeviation());
  }
}
