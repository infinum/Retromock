package co.infinum.retromock

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RetromockBodyFactoryTest {

    @Mock
    lateinit var bodyFactory: BodyFactory

    @Test
    fun bodyFactoryCalled() {
        val input = "testInput"
        val retromockBodyFactory = RetromockBodyFactory(bodyFactory, input)

        retromockBodyFactory.createBody()

        verify(bodyFactory).create(input)
        verifyNoMoreInteractions(bodyFactory)
    }

    @Test
    fun bodyFactoryCalledMultipleTimes() {
        val input = "testInput"
        val retromockBodyFactory = RetromockBodyFactory(bodyFactory, input)

        retromockBodyFactory.createBody()
        retromockBodyFactory.createBody()

        verify(bodyFactory, times(2)).create(input)
        verifyNoMoreInteractions(bodyFactory)
    }

}