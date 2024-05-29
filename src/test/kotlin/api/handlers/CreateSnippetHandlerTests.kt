package api.handlers

import api.configuration.serialization.moshi
import api.dtos.CreateSnippetDto
import api.validation.SnippetValidator
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import domain.Snippet
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.junit.jupiter.api.Test
import repository.SnippetRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isLessThan
import java.time.Duration
import java.time.Instant

class CreateSnippetHandlerTests {
    private val repository = mockk<SnippetRepository>()
    private val validator = mockk<SnippetValidator>()

    private val handler = CreateSnippetHandler(repository, moshi, validator)
    private val routing = "/" bind Method.POST to handler::invoke

    @Test
    fun `Request for valid snippet responds with HTTP 201 and created snippet`() {
        every { repository.insert(any()) } returns Unit.right()
        val slot = slot<Snippet>()
        every { validator.validate(capture(slot)) } answers { slot.captured.right() }

        val response = routing.invoke(
            Request(Method.POST, "/")
                .body(moshi.asFormatString(snippetDto))
        )

        expectThat(response.status)
            .isEqualTo(Status.CREATED)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)

        val snippet = moshi.asA<Snippet>(response.bodyString())
        expectThat(snippet.title)
            .isEqualTo(snippetDto.title)
        expectThat(snippet.contents)
            .isEqualTo(snippetDto.contents)
        expectThat(snippet.expiresAt)
            .isEqualTo(snippet.expiresAt)
        expectThat(snippet.createdAt)
            .isLessThan(Instant.now())
            .isGreaterThan(Instant.now().minusSeconds(60))
    }

    @Test
    fun `Request for invalid snippet JSON responds with HTTP 400 and invalid JSON error`() {
        val response = routing.invoke(
            Request(Method.POST, "/")
                .body("Snippet")
        )

        expectThat(response.status)
            .isEqualTo(Status.BAD_REQUEST)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"invalid_snippet_json","status":400,"message":"Invalid snippet JSON"}""")
    }

    @Test
    fun `Request for snippet with single validation error responds with HTTP 400 and validation error`() {
        every { repository.insert(any()) } returns Unit.right()
        every { validator.validate(any()) } answers { NonEmptyList<SnippetValidator.SnippetValidationError>(SnippetValidator.SnippetValidationError.ContentsCantBeEmpty(), emptyList()).left() }

        val response = routing.invoke(
            Request(Method.POST, "/")
                .body(moshi.asFormatString(snippetDto.copy(contents = "")))
        )

        expectThat(response.status)
            .isEqualTo(Status.BAD_REQUEST)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"invalid_snippet","errors":["Contents cannot be empty"],"status":400,"message":"Invalid snippet"}""")
    }

    @Test
    fun `Request for snippet with multiple validation error responds with HTTP 400 and validation error`() {
        every { repository.insert(any()) } returns Unit.right()
        every { validator.validate(any()) } answers {
            NonEmptyList(
                SnippetValidator.SnippetValidationError.ContentsCantBeEmpty(),
                listOf(SnippetValidator.SnippetValidationError.ExpiryDateMustBeFuture())
            )
                .left()
        }

        val response = routing.invoke(
            Request(Method.POST, "/")
                .body(moshi.asFormatString(snippetDto.copy(contents = "")))
        )

        expectThat(response.status)
            .isEqualTo(Status.BAD_REQUEST)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"invalid_snippet","errors":["Contents cannot be empty","Expiry date must be in the future"],"status":400,"message":"Invalid snippet"}""")
    }
}

private val snippetDto = CreateSnippetDto(
    title = "title",
    contents = "contents",
    expiresAt = Instant.now().plus(Duration.ofDays(1)),
)
