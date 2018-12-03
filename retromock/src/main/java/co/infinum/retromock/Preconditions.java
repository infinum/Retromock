package co.infinum.retromock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class Preconditions {

  private Preconditions() {
  }

  static void checkNotNull(@Nullable Object object, String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  static void checkNotEmpty(@Nonnull Object[] array, String message) {
    if (array.length == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  static void checkNotNullOrEmpty(@Nullable Object[] array, String message) {
    checkNotNull(array, message);
    checkNotEmpty(array, message);
  }
}
