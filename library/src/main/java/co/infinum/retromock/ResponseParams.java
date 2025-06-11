package co.infinum.retromock;

import javax.annotation.Nullable;

import okhttp3.Headers;

final class ResponseParams {

  /**
   * HTTP status code for the response.
   */
  private final int code;

  /**
   * HTTP status message for the response.
   */
  private final String message;

  /**
   * Factory for creating the response body.
   */
  private final RetromockBodyFactory bodyFactory;

  /**
   * HTTP headers for the response.
   */
  private final Headers headers;

  private ResponseParams(final Builder builder) {
    this(
      builder.code,
      builder.message,
      builder.bodyFactory,
      builder.headers
    );
  }

  ResponseParams(
    final int code,
    final String message,
    final RetromockBodyFactory bodyFactory,
    final Headers headers) {

    this.code = code;
    this.message = message;
    this.bodyFactory = bodyFactory;
    this.headers = headers;
  }

  int code() {
    return code;
  }

  String message() {
    return message;
  }

  @Nullable
  RetromockBodyFactory bodyFactory() {
    return bodyFactory;
  }

  Headers headers() {
    return headers;
  }

  @Nullable
  String contentType() {
    return contentTypeInternal(headers);
  }

  long contentLength() {
    return contentLengthInternal(headers);
  }

  Builder newBuilder() {
    return new Builder(this);
  }

  static final class Builder {

    /**
     * HTTP status code for the response.
     */
    private int code;

    /**
     * HTTP status message for the response.
     */
    private String message;

    /**
     * Factory for creating the response body.
     */
    private RetromockBodyFactory bodyFactory;

    /**
     * HTTP headers for the response.
     */
    private Headers headers;

    Builder() {
    }

    private Builder(final ResponseParams params) {
      this.code = params.code;
      this.message = params.message;
      this.bodyFactory = params.bodyFactory;
      this.headers = params.headers;
    }

    Builder code(final int code) {
      this.code = code;
      return this;
    }

    Builder message(final String message) {
      this.message = message;
      return this;
    }

    Builder bodyFactory(@Nullable final RetromockBodyFactory bodyFactory) {
      this.bodyFactory = bodyFactory;
      return this;
    }

    Builder headers(@Nullable final Headers headers) {
      this.headers = headers;
      return this;
    }

    ResponseParams build() {
      if (message == null) {
        message = "";
      }
      if (headers == null) {
        headers = Headers.of();
      }
      return new ResponseParams(this);
    }

    @Nullable
    String contentType() {
      return contentTypeInternal(headers);
    }

    long contentLength() {
      return contentLengthInternal(headers);
    }
  }

  @Nullable
  private static String contentTypeInternal(final Headers headers) {
    String value = headers.get("Content-Type");
    if (value != null) {
      return value;
    } else {
      return null;
    }
  }

  private static long contentLengthInternal(final Headers headers) {
    String value = headers.get("Content-Length");
    if (value == null) {
      return -1;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
