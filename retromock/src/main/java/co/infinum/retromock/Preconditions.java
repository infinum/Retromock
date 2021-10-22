package co.infinum.retromock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class Preconditions {

  private Preconditions() {
  }

  static void checkNotNull(
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    @Nullable final Object object,
    final String message
  ) {
    if (object == null) {
      throw new NullPointerException(message);
    }
  }

  static void checkNotEmpty(@Nonnull final Object[] array, final String message) {
    if (array.length == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  static void checkNotNullOrEmpty(
    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    @Nullable final Object[] array,
    final String message
  ) {
    checkNotNull(array, message);
    checkNotEmpty(array, message);
  }
}
