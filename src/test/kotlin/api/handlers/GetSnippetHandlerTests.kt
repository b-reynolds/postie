package api.handlers

import api.PathParam
import api.configuration.moshi
import api.handlers.GetSnippetHandler
import arrow.core.left
import arrow.core.right
import domain.Snippet
import io.mockk.every
import io.mockk.mockk
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
import java.time.Instant
import java.util.UUID

class GetSnippetHandlerTests {
    private val repository = mockk<SnippetRepository>()
    private val handler = GetSnippetHandler(repository, moshi)
    private val routing = "/{${PathParam.SNIPPET_ID}}" bind Method.GET to handler::invoke

    @Test
    fun `Request for snippet that exists responds with HTTP 200`() {
        every { repository.get(snippet.id) } returns snippet.right()
        val response = routing.invoke(Request(Method.GET, "/${snippet.id}"))

        expectThat(response.status)
            .isEqualTo(Status.OK)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo(moshi.asFormatString(snippet))
    }

    @Test
    fun `Request for snippet that doesn't exist responds with HTTP 404`() {
        every { repository.get(snippet.id) } returns SnippetRepository.Error.GetItemError.NotFound(snippet.id).left()
        val response = routing.invoke(Request(Method.GET, "/${snippet.id}"))

        expectThat(response.status)
            .isEqualTo(Status.NOT_FOUND)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"snippet_not_found","status":404,"message":"Snippet not found with id: ${snippet.id}"}""")
    }

    @Test
    fun `Request for snippet with invalid UUID responds with HTTP 400`() {
        val snippetId = "snippetId"
        val response = routing.invoke(Request(Method.GET, "/$snippetId"))

        expectThat(response.status)
            .isEqualTo(Status.BAD_REQUEST)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"invalid_snippet_id","status":400,"message":"Invalid snippet ID: $snippetId"}""")
    }

    @Test
    fun `Request for snippet with missing snippet ID path parameter responds with HTTP 400`() {
        val misconfiguredRouting = "/" bind Method.GET to handler::invoke
        val response = misconfiguredRouting.invoke(Request(Method.GET, "/"))

        expectThat(response.status)
            .isEqualTo(Status.BAD_REQUEST)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"missing_path_param","status":400,"message":"Missing path parameter: id"}""")
    }

    @Test
    fun `Request for snippet resulting in unexpected repository error responds with HTTP 500`() {
        val failure = "expect the unexpected"
        every { repository.get(snippet.id) } returns SnippetRepository.Error.GetItemError.Failure(failure).left()

        val response = routing.invoke(Request(Method.GET, "/${snippet.id}"))

        expectThat(response.status)
            .isEqualTo(Status.INTERNAL_SERVER_ERROR)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"internal_server_error","message":"$failure","status":500}""")
    }

    @Test
    fun `Request for snippet resulting in unexpected exception responds with HTTP 500`() {
        val exception = RuntimeException("expected the unexpected")
        every { repository.get(snippet.id) } throws exception

        val response = routing.invoke(Request(Method.GET, "/${snippet.id}"))

        expectThat(response.status)
            .isEqualTo(Status.INTERNAL_SERVER_ERROR)
        expectThat(response.contentType())
            .isEqualTo(ContentType.APPLICATION_JSON)
        expectThat(response.bodyString())
            .isEqualTo("""{"error":"internal_server_error","message":"${exception.message}","status":500}""")
    }
}

private val snippet = Snippet(
    id = UUID.randomUUID(),
    title = "title",
    contents = "contents",
    createdAt = Instant.now()
)
