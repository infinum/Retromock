@file:JvmName("-Utils")

package co.infinum.retromock.helpers

import co.infinum.retromock.Behavior
import co.infinum.retromock.BodyFactory
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

fun <T> whenever(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)
inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)
inline fun <reified T : Any> captor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)
class EmptyBodyFactory : BodyFactory {
    override fun create(input: String): InputStream {
        throw NotImplementedError("Method shouldn't be called.")
    }
}

class EmptyBodyFactory2 : BodyFactory {
    override fun create(input: String): InputStream {
        throw NotImplementedError("Method shouldn't be called.")
    }
}

class ImmediateBehavior : Behavior {
    override fun delayMillis(): Long = 0L
}

class CountDownBodyFactory(
    private val counter: AtomicInteger = AtomicInteger(1)
) : BodyFactory {
    override fun create(input: String): InputStream {
        counter.decrementAndGet()
        return input.byteInputStream(StandardCharsets.UTF_8)
    }
}