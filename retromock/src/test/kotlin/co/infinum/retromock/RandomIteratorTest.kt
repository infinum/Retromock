package co.infinum.retromock

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RandomIteratorTest {

    @Mock
    internal lateinit var randomProvider: RandomProvider

    @Test
    fun iterateThroughAllRandomResponses() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = RandomIterator<Any>(responses)

        assertThat(iterator.next()).isIn(*responses)
        assertThat(iterator.next()).isIn(*responses)
        assertThat(iterator.next()).isIn(*responses)
    }

    @Test
    fun repeatAfterLastRandomResponse() {
        val responses = arrayOf(Any(), Any(), Any())

        val iterator = RandomIterator<Any>(responses)

        iterator.next()
        iterator.next()

        assertThat(iterator.next()).isIn(*responses)
        assertThat(iterator.next()).isIn(*responses)
        assertThat(iterator.next()).isIn(*responses)
    }

    @Test
    fun emptyArrayThrows() {
        val responses = arrayOf<Any>()

        assertThrows<IllegalArgumentException> {
            RandomIterator<Any>(responses)
        }
    }

    @Test
    fun iterateThroughAllResponses() {
        val responses = arrayOf(Any(), Any(), Any())

        `when`(randomProvider.nextInt(3)).thenReturn(0, 2, 1)

        val iterator = RandomIterator<Any>(responses, randomProvider)

        assertThat(iterator.next()).isSameAs(responses[0])
        assertThat(iterator.next()).isSameAs(responses[2])
        assertThat(iterator.next()).isSameAs(responses[1])

        verify(randomProvider, times(3)).nextInt(3)
    }

    @Test
    fun repeatAfterLastResponse() {
        val responses = arrayOf(Any(), Any(), Any())

        `when`(randomProvider.nextInt(3)).thenReturn(0, 2, 1, 2, 1)

        val iterator = RandomIterator<Any>(responses, randomProvider)

        iterator.next()
        iterator.next()

        assertThat(iterator.next()).isSameAs(responses[1])
        assertThat(iterator.next()).isSameAs(responses[2])
        assertThat(iterator.next()).isSameAs(responses[1])

        verify(randomProvider, times(5)).nextInt(3)
    }

}