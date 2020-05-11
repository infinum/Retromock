package co.infinum.retromock;

import java.lang.reflect.Type;

class NoOpCallWrapper implements CallWrapper {

  private Type returnType;

  public NoOpCallWrapper(final Type returnType) {
    this.returnType = returnType;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public Object wrap(final Object call) {
    return call;
  }
}
