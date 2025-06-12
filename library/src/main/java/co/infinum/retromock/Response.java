package co.infinum.retromock;

import java.net.HttpURLConnection;

import okhttp3.Headers;

/**
 * Use this class to specify parameters to define a mocked response.
 * This class is equivalent of @{@link co.infinum.retromock.meta.MockResponse}.
 */
public final class Response {

  /**
   * HTTP status code for the response.
   */
  private final int code;

  /**
   * HTTP status message for the response.
   */
  private final String message;

  /**
   * Response body content or body factory specifier.
   */
  private final String body;

  /**
   * HTTP headers for the response.
   */
  private final Headers headers;

  /**
   * Class used to create the response body from the body string.
   */
  private final Class<? extends BodyFactory> bodyFactoryClass;

  Response(
    final int code,
    final String message,
    final String body,
    final Headers headers,
    final Class<? extends BodyFactory> bodyFactoryClass) {

    this.code = code;
    this.message = message;
    this.body = body;
    this.headers = headers;
    this.bodyFactoryClass = bodyFactoryClass;
  }

  int code() {
    return code;
  }

  String message() {
    return message;
  }

  String body() {
    return body;
  }

  Headers headers() {
    return headers;
  }

  Class<? extends BodyFactory> bodyFactoryClass() {
    return bodyFactoryClass;
  }

  /**
   * Build a new {@link Response} instance.
   * All methods are optional.
   */
  public static final class Builder {

    /**
     * HTTP status code, defaults to 200 (OK).
     */
    private int code = HttpURLConnection.HTTP_OK;

    /**
     * HTTP status message.
     */
    private String message;

    /**
     * Response body content or body factory specifier.
     */
    private String body;

    /**
     * HTTP headers for the response.
     */
    private Headers headers;

    /**
     * Class used to create the response body from the body string.
     */
    private Class<? extends BodyFactory> bodyFactoryClass;

    /**
     * Optionally set HTTP status code, default value is <code>200</code>.
     *
     * @param code Status code
     * @return This builder
     */
    public Builder code(final int code) {
      this.code = code;
      return this;
    }

    /**
     * Optionally set HTTP status message, default value is <code>OK</code>.
     *
     * @param message Status message
     * @return This builder
     */
    public Builder message(final String message) {
      this.message = message;
      return this;
    }

    /**
     * Optionally set body specifier, default is empty string.
     * This could be either plain response body or a specification
     * for {@link BodyFactory} class. Depending on bodyFactory parameter provided this will be
     * parsed in a different way.
     *
     * @param body Body specifier.
     * @return This builder.
     * @see BodyFactory
     */
    public Builder body(final String body) {
      this.body = body;
      return this;
    }

    /**
     * Optionally set HTTP headers, default is empty array.
     *
     * @param headers Array of response headers.
     * @return This builder.
     */
    public Builder headers(final Headers headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Optionally set a class used to convert body parameter specifier to
     * {@link java.io.InputStream} body. Note: instance of class provided here has to be registered
     * to {@link Retromock} using {@code addBodyParser} method.
     *
     * @param bodyFactoryClass BodyFactory class used to convert body parameter text to body.
     * @return This builder.
     */
    public Builder bodyFactory(final Class<? extends BodyFactory> bodyFactoryClass) {
      this.bodyFactoryClass = bodyFactoryClass;
      return this;
    }

    /**
     * Creates a new instance of {@link Response} using the configured values.
     *
     * @return A new instance of {@link Response}.
     */
    public Response build() {
      String message = this.message;
      if (message == null) {
        message = "OK";
      }
      String body = this.body;
      if (body == null) {
        body = "";
      }
      Headers headers = this.headers;
      if (headers == null) {
        headers = Headers.of();
      }
      Class<? extends BodyFactory> bodyFactoryClass = this.bodyFactoryClass;
      if (bodyFactoryClass == null) {
        bodyFactoryClass = BodyFactory.class;
      }

      return new Response(code, message, body, headers, bodyFactoryClass);
    }
  }
}
