package co.infinum.retromock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class DefaultBehaviorTest {

    @Mock
    private lateinit var randomProvider: RandomProvider

    @Test
    fun delayIsMinWhenGeneratesZero() {
        val mean = 1000L
        val deviation = 500
        `when`(randomProvider.nextLong((deviation * 2).toLong())).thenReturn(0)

        val behavior = DefaultBehavior(mean, deviation, randomProvider)
        val delay = behavior.delayMillis()

        assertThat(delay).isEqualTo(mean - deviation)
    }

    @Test
    fun delayIsMeanWhenGeneratesDeviation() {
        val mean = 1000L
        val deviation = 500
        `when`(randomProvider.nextLong((deviation * 2).toLong())).thenReturn(deviation.toLong())

        val behavior = DefaultBehavior(mean, deviation, randomProvider)
        val delay = behavior.delayMillis()

        assertThat(delay).isEqualTo(mean)
    }

    @Test
    fun delayIsMaxWhenGeneratesDoubleDeviation() {
        val mean = 1000L
        val deviation = 500
        `when`(randomProvider.nextLong((deviation * 2).toLong())).thenReturn((deviation * 2).toLong())

        val behavior = DefaultBehavior(mean, deviation, randomProvider)
        val delay = behavior.delayMillis()

        assertThat(delay).isEqualTo(mean + deviation)
    }

    @Test
    fun delayIsInRange() {
        //TODO: this isn't really the smartest test as it is not reproducible
        val mean = 1000L
        val deviation = 500

        val behavior = DefaultBehavior(mean, deviation)
        val delay = behavior.delayMillis()

        assertThat(delay).isBetween(mean - deviation, mean + deviation)
    }

    @Test
    fun delayCallsRandomProviderWithCorrectRange() {
        val mean = 1000L
        val deviation = 500

        val behavior = DefaultBehavior(mean, deviation, randomProvider)
        behavior.delayMillis()

        verify(randomProvider).nextLong(1000)
    }

    @Test
    fun delayWithDeviationZeroReturnsMean() {
        val mean = 1000L
        val deviation = 0

        val behavior = DefaultBehavior(mean, deviation, randomProvider)
        val delay = behavior.delayMillis()

        assertThat(delay).isEqualTo(mean)
        verifyNoMoreInteractions(randomProvider)
    }

    @Test
    fun negativeDeviationThrows() {
        assertThrows<IllegalArgumentException> {
            DefaultBehavior(100, -1, randomProvider)
        }
    }

}