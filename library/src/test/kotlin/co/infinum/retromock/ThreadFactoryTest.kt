package co.infinum.retromock

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ThreadFactoryTest {

    @Test
    fun threadIsDaemon() {
        val factory = DefaultThreadFactory()

        val thread = factory.newThread({ })

        assertTrue { thread.isDaemon }
    }

    @Test
    fun threadName() {
        val size = DefaultThreadFactory.POOL_INDEX.get()

        val factory = DefaultThreadFactory()

        val thread = factory.newThread({ })
        val thread2 = factory.newThread({ })
        val thread3 = factory.newThread({ })

        assertThat(thread.name).isEqualTo("Retromock-$size-thread")
        assertThat(thread2.name).isEqualTo("Retromock-${size + 1}-thread")
        assertThat(thread3.name).isEqualTo("Retromock-${size + 2}-thread")
    }

}