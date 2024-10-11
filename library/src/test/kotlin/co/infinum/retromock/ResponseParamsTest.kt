package co.infinum.retromock

import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ResponseParamsTest {

    @Mock
    private lateinit var bodyFactory: BodyFactory

    private lateinit var retromockBodyFactory: RetromockBodyFactory;

    @BeforeEach
    fun setup() {
        retromockBodyFactory = RetromockBodyFactory(bodyFactory, "")
    }

    @Test
    fun parseContentType() {
        val headers = Headers.of(mapOf(
            "Content-Type" to "text/plain"
        ))
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentType()

        assertThat(contentType).isEqualTo("text/plain")
    }

    @Test
    fun parseNoContentType() {
        val headers = Headers.of(mapOf(
            "Content-Type2" to "text/plain"
        ))
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentType()

        assertThat(contentType).isNull()
    }

    @Test
    fun parseEmptyContentType() {
        val headers = Headers.of(mapOf())
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentType()

        assertThat(contentType).isNull()
    }

    @Test
    fun parseContentLength() {
        val headers = Headers.of(mapOf(
            "Content-Length" to "1024"
        ))
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentLength()

        assertThat(contentType).isEqualTo(1024)
    }

    @Test
    fun parseNoContentLength() {
        val headers = Headers.of(mapOf(
            "Content-Length2" to "1024"
        ))
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentLength()

        assertThat(contentType).isEqualTo(-1)
    }

    @Test
    fun parseEmptyContentLength() {
        val headers = Headers.of(mapOf())
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val contentType = params.contentLength()

        assertThat(contentType).isEqualTo(-1)
    }

    @Test
    fun codeIsEqual() {
        val code = 200
        val params = ResponseParams.Builder()
            .code(code)
            .build()

        val actual = params.code()

        assertThat(actual).isEqualTo(code)
    }

    @Test
    fun messageIsEqual() {
        val message = "OK"
        val params = ResponseParams.Builder()
            .message(message)
            .build()

        val actual = params.message()

        assertThat(actual).isEqualTo(message)
    }

    @Test
    fun bodyFactoryIsEqual() {
        val params = ResponseParams.Builder()
            .bodyFactory(retromockBodyFactory)
            .build()

        val actual = params.bodyFactory()

        assertThat(actual).isSameAs(retromockBodyFactory)
    }

    @Test
    fun headersAreEqual() {
        val headers = Headers.of()
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val actual = params.headers()

        assertThat(actual).isSameAs(headers)
    }

    @Test
    fun nullHeadersToEmpty() {
        val headers: Headers? = null
        val params = ResponseParams.Builder()
            .headers(headers)
            .build()

        val actual = params.headers()

        assertThat(actual).isEqualTo(Headers.of())
    }

    @Test
    fun copyHasSameValuesAsInstance() {
        val params = ResponseParams.Builder()
            .code(200)
            .message("OK")
            .bodyFactory(retromockBodyFactory)
            .headers(Headers.of())
            .build()

        val copy = params.newBuilder().build()

        assertThat(copy.code()).isEqualTo(params.code())
        assertThat(copy.message()).isEqualTo(params.message())
        assertThat(copy.bodyFactory()).isSameAs(params.bodyFactory())
        assertThat(copy.headers()).isSameAs(params.headers())
        assertThat(copy.contentType()).isEqualTo(params.contentType())
        assertThat(copy.contentLength()).isEqualTo(params.contentLength())
    }

    @Test
    fun builderParsesSameAsInstance() {
        val params = ResponseParams.Builder()
            .headers(Headers.of(mapOf(
                "Content-Type" to "text/plain",
                "Content-Length" to "1024"
            )))
            .build()

        val builder = params.newBuilder()

        assertThat(builder.contentType()).isEqualTo(params.contentType())
        assertThat(builder.contentLength()).isEqualTo(params.contentLength())
    }

    @Test
    fun invalidContentLengthProducesMinus1() {
        val params = ResponseParams.Builder()
            .headers(Headers.of(mapOf(
                "Content-Length" to "IamNotANumber"
            )))
            .build()

        assertThat(params.contentLength()).isEqualTo(-1)
    }

}