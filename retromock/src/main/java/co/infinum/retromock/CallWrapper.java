package co.infinum.retromock;

import java.lang.reflect.Type;

interface CallWrapper {

  Object wrap(final Object call);

  Type getReturnType();
}