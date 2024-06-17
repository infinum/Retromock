package co.infinum.retromock

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets

class NoResponseProducerTest {

    @Test
    fun producesEmptyResponse() {
        val producer = NoResponseProducer(
            Retromock.Builder()
                .retrofit(Retrofit.Builder()
                    .baseUrl("http://infinum.co")
                    .build()
                )
                .build(),
            ResponseParams.Builder()
                .build()
        )

        val params = producer.produce(arrayOf())

        val bodyFactory = params.bodyFactory()

        assertThat(bodyFactory).isNotNull()
        assertThat(bodyFactory!!.createBody().readBytes().toString(StandardCharsets.UTF_8)).isEqualTo("")
    }

}