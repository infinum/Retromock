package co.infinum.retromock;

import javax.annotation.Nullable;

final class Preconditions {

  static void checkNotNull(@Nullable Object object, String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }
}
