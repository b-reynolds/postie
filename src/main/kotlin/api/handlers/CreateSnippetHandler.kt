package api.handlers

import api.dtos.CreateSnippetDto
import api.handlers.GetSnippetHandler.Error
import api.validation.SnippetValidator
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import domain.Snippet
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.format.ConfigurableMoshi
import org.http4k.lens.contentType
import repository.SnippetRepository
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.Polymorphic
import se.ansman.kotshi.PolymorphicLabel
import java.time.Instant
import java.util.UUID

class CreateSnippetHandler(
    private val repository: SnippetRepository,
    private val moshi: ConfigurableMoshi,
    private val snippetValidator: SnippetValidator
) : HttpHandler {
    override fun invoke(request: Request) =
        catch({ handle(request)}) { t ->
            Error.InternalServerError(t.message.orEmpty())
                .left()
        }
            .toResponse()

    private fun handle(request: Request) = either {
        val snippet = validate(request.snippet.bind()).bind()
        create(snippet).bind()
    }

    private fun create(snippet: Snippet): Either<Error.InternalServerError, Snippet> =
        repository
            .insert(snippet)
            .mapLeft { error -> Error.InternalServerError(error.message) }
            .map { snippet }

    private fun validate(snippet: Snippet): Either<Error.InvalidSnippet, Snippet> =
        snippetValidator
            .validate(snippet)
            .mapLeft { errors -> Error.InvalidSnippet(errors.map { error -> error.description }) }

    private fun Either<Error, Snippet>.toResponse() = fold(
        ifLeft = { error ->
            Response(error.status)
                .contentType(ContentType.APPLICATION_JSON)
                .body(moshi.asFormatString(error))
        },
        ifRight = { snippet ->
            Response(CREATED)
                .contentType(ContentType.APPLICATION_JSON)
                .body(moshi.asFormatString(snippet))
        }
    )

    private val Request.snippet: Either<Error, Snippet>
        get() = either {
            catch({ moshi.asA<CreateSnippetDto>(body.stream)}) {
                raise(Error.InvalidSnippetJson())
            }
                .toSnippet()
        }

    private fun CreateSnippetDto.toSnippet() = Snippet(
        id = UUID.randomUUID(),
        title = title,
        contents = contents,
        createdAt = Instant.now(),
        format = format,
        expiresAt = expiresAt
    )

    @JsonSerializable
    @Polymorphic("error")
    internal sealed class Error(
        open val status: Status,
        open val message: String
    ) {
        @JsonSerializable
        @PolymorphicLabel("internal_server_error")
        data class InternalServerError(
            override val message: String,
            override val status: Status = INTERNAL_SERVER_ERROR,
        ) : Error(status, message)

        @JsonSerializable
        @PolymorphicLabel("invalid_snippet_json")
        data class InvalidSnippetJson(
            override val status: Status = BAD_REQUEST,
            override val message: String = "Invalid snippet JSON"
        ) : Error(status, message)

        @JsonSerializable
        @PolymorphicLabel("invalid_snippet")
        data class InvalidSnippet(
            val errors: List<String>,
            override val status: Status = BAD_REQUEST,
            override val message: String = "Invalid snippet"
        ) : Error(status, message)
    }
}
