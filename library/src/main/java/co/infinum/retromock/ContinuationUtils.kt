package co.infinum.retromock

import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.coroutines.Continuation

val Method.actualParameterTypes: List<Type>
    get() {
        val params = genericParameterTypes.toMutableList()
        val lastParam = params.lastOrNull() ?: return params
        if (Utils.getRawType(lastParam) == Continuation::class.java) {
            params.remove(lastParam)
        }

        return params
    }

fun Class<*>.getDeclaredSuspendMethod(name: String, vararg params: Class<*>): Method {
    val allParams = params.toList() + Continuation::class.java

    return getDeclaredMethod(name, *allParams.toTypedArray())
}