package co.infinum.retromock

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SequentialIteratorTest {

    @Test
    fun iterateThroughAllResponses() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = SequentialIterator<Any>(responses)

        assertThat(iterator.next()).isSameAs(responses[0])
        assertThat(iterator.next()).isSameAs(responses[1])
        assertThat(iterator.next()).isSameAs(responses[2])
    }

    @Test
    fun repeatAfterLastResponse() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = SequentialIterator<Any>(responses)

        iterator.next()

        assertThat(iterator.next()).isSameAs(responses[1])
        assertThat(iterator.next()).isSameAs(responses[2])
        assertThat(iterator.next()).isSameAs(responses[2])
        assertThat(iterator.next()).isSameAs(responses[2])
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyArrayThrows() {
        val responses = arrayOf<Any>()

        SequentialIterator<Any>(responses)
    }

}