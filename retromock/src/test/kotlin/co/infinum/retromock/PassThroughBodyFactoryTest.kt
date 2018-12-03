package co.infinum.retromock

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.nio.charset.StandardCharsets

@RunWith(MockitoJUnitRunner::class)
class PassThroughBodyFactoryTest {

    @Test
    fun streamHasSameContentAsInput() {
        val input = "This is just an example."
        val bodyFactory = PassThroughBodyFactory()

        val stream = bodyFactory.create(input)

        val output = stream.readBytes().toString(StandardCharsets.UTF_8)

        assertThat(output).isEqualTo(input)
    }

}