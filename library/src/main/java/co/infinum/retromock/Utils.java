/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.infinum.retromock;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

final class Utils {
  private Utils() {
    // No instances.
  }

  static Class<?> getRawType(final Type type) {
    Objects.requireNonNull(type, "type == null");

    if (type instanceof Class<?>) {
      // Type is a normal class.
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
      // suspects some pathological case related to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
        throw new IllegalArgumentException();
      }
      return (Class<?>) rawType;
    }
    if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();
    }
    if (type instanceof TypeVariable) {
      // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
      // type that's more general than necessary is okay.
      return Object.class;
    }
    if (type instanceof WildcardType) {
      return getRawType(((WildcardType) type).getUpperBounds()[0]);
    }

    throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
          + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
  }

  /**
   * Returns true if {@code a} and {@code b} are equal.
   *
   * @param a the first type to compare
   * @param b the second type to compare
   * @return true if the types are equal, false otherwise
   */
  static boolean equals(final Type a, final Type b) {
    if (a == b) {
      return true; // Also handles (a == null && b == null).

    } else if (a instanceof Class) {
      return a.equals(b); // Class already specifies equals().

    } else if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType)) {
        return false;
      }
      ParameterizedType pa = (ParameterizedType) a;
      ParameterizedType pb = (ParameterizedType) b;
      Object ownerA = pa.getOwnerType();
      Object ownerB = pb.getOwnerType();
      return (ownerA == ownerB || (ownerA != null && ownerA.equals(ownerB)))
          && pa.getRawType().equals(pb.getRawType())
          && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

    } else if (a instanceof GenericArrayType) {
      if (!(b instanceof GenericArrayType)) {
        return false;
      }
      GenericArrayType ga = (GenericArrayType) a;
      GenericArrayType gb = (GenericArrayType) b;
      return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

    } else if (a instanceof WildcardType) {
      if (!(b instanceof WildcardType)) {
        return false;
      }
      WildcardType wa = (WildcardType) a;
      WildcardType wb = (WildcardType) b;
      return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
          && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

    } else if (a instanceof TypeVariable) {
      if (!(b instanceof TypeVariable)) {
        return false;
      }
      TypeVariable<?> va = (TypeVariable<?>) a;
      TypeVariable<?> vb = (TypeVariable<?>) b;
      return va.getGenericDeclaration() == vb.getGenericDeclaration()
          && va.getName().equals(vb.getName());

    } else {
      return false; // This isn't a type we support!
    }
  }

  static String typeToString(final Type type) {
    if (type instanceof Class) {
      return ((Class<?>) type).getName();
    }
    return type.toString();
  }

  static void checkNotPrimitive(final Type type) {
    if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
      throw new IllegalArgumentException();
    }
  }

  static Type getParameterUpperBound(final int index, final ParameterizedType type) {
    Type[] types = type.getActualTypeArguments();
    if (index < 0 || index >= types.length) {
      throw new IllegalArgumentException(
          "Index " + index + " not in range [0," + types.length + ") for " + type);
    }
    Type paramType = types[index];
    if (paramType instanceof WildcardType) {
      return ((WildcardType) paramType).getUpperBounds()[0];
    }
    return paramType;
  }

  static Type getParameterLowerBound(final int index, final ParameterizedType type) {
    Type paramType = type.getActualTypeArguments()[index];
    if (paramType instanceof WildcardType) {
      return ((WildcardType) paramType).getLowerBounds()[0];
    }
    return paramType;
  }

  /**
   * Default capacity for StringBuilder instances in string formatting.
   */
  static final int STRING_BUILDER_CAPACITY = 30;

  static final class ParameterizedTypeImpl implements ParameterizedType {
    /**
     * The owner type of this parameterized type, or null if none.
     */
    private final @Nullable
    Type ownerType;

    /**
     * The raw type of this parameterized type.
     */
    private final Type rawType;

    /**
     * The type arguments for this parameterized type.
     */
    private final Type[] typeArguments;

    ParameterizedTypeImpl(final @Nullable Type ownerType, final Type rawType, final Type... typeArguments) {
      // Require an owner type if the raw type needs it.
      if (rawType instanceof Class<?>
          && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
        throw new IllegalArgumentException();
      }

      for (Type typeArgument : typeArguments) {
        Objects.requireNonNull(typeArgument, "typeArgument == null");
        checkNotPrimitive(typeArgument);
      }

      this.ownerType = ownerType;
      this.rawType = rawType;
      this.typeArguments = typeArguments.clone();
    }

    @Override public Type[] getActualTypeArguments() {
      return typeArguments.clone();
    }

    @Override public Type getRawType() {
      return rawType;
    }

    @Override public @Nullable
    Type getOwnerType() {
      return ownerType;
    }

    @Override public boolean equals(final Object other) {
      return other instanceof ParameterizedType && Utils.equals(this, (ParameterizedType) other);
    }

    @Override public int hashCode() {
      if (ownerType != null) {
        return Arrays.hashCode(typeArguments)
          ^ rawType.hashCode()
          ^ ownerType.hashCode();
      }
      return Arrays.hashCode(typeArguments)
        ^ rawType.hashCode();
    }

    @Override public String toString() {
      if (typeArguments.length == 0) {
        return typeToString(rawType);
      }
      StringBuilder result = new StringBuilder(STRING_BUILDER_CAPACITY * (typeArguments.length + 1));
      result.append(typeToString(rawType)).append('<').append(typeToString(typeArguments[0]));
      for (int i = 1; i < typeArguments.length; i++) {
        result.append(", ").append(typeToString(typeArguments[i]));
      }
      return result.append(">").toString();
    }
  }
}
