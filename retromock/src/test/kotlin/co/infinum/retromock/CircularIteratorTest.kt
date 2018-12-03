package co.infinum.retromock

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CircularIteratorTest {

    @Test
    fun iterateThroughAllResponses() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = CircularIterator<Any>(responses)

        assertThat(iterator.next()).isSameAs(responses[0])
        assertThat(iterator.next()).isSameAs(responses[1])
        assertThat(iterator.next()).isSameAs(responses[2])
    }

    @Test
    fun repeatAfterLastResponse() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = CircularIterator<Any>(responses)

        iterator.next()
        iterator.next()

        assertThat(iterator.next()).isSameAs(responses[2])
        assertThat(iterator.next()).isSameAs(responses[0])
        assertThat(iterator.next()).isSameAs(responses[1])
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyArrayThrows() {
        val responses = arrayOf<Any>()

        CircularIterator<Any>(responses)
    }

}