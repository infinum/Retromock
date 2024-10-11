package co.infinum.retromock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class Preconditions {

  private Preconditions() {
  }

  static void checkNotNull(@Nullable final Object object, final String message) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  static void checkNotEmpty(@Nonnull final Object[] array, final String message) {
    if (array.length == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  static void checkNotNullOrEmpty(@Nullable final Object[] array, final String message) {
    checkNotNull(array, message);
    checkNotEmpty(array, message);
  }
}
