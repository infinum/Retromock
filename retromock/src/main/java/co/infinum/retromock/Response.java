package co.infinum.retromock;

import java.net.HttpURLConnection;

import okhttp3.Headers;

public final class Response {

  private final int code;
  private final String message;
  private final String body;
  private final Headers headers;
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

  public static final class Builder {

    private int code = HttpURLConnection.HTTP_OK;
    private String message;
    private String body;
    private Headers headers;
    private Class<? extends BodyFactory> bodyFactoryClass;

    public Builder code(final int code) {
      this.code = code;
      return this;
    }

    public Builder message(final String message) {
      this.message = message;
      return this;
    }

    public Builder body(final String body) {
      this.body = body;
      return this;
    }

    public Builder headers(final Headers headers) {
      this.headers = headers;
      return this;
    }

    public Builder bodyFactory(final Class<? extends BodyFactory> bodyFactoryClass) {
      this.bodyFactoryClass = bodyFactoryClass;
      return this;
    }

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
