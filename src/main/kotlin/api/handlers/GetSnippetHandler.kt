package api.handlers

import api.PathParam
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import com.squareup.moshi.Json
import domain.Snippet
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.ConfigurableMoshi
import org.http4k.lens.contentType
import org.http4k.routing.path
import repository.SnippetRepository
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.Polymorphic
import se.ansman.kotshi.PolymorphicLabel
import java.util.UUID

class GetSnippetHandler(
    private val repository: SnippetRepository,
    private val moshi: ConfigurableMoshi
) : HttpHandler {
    override fun invoke(request: Request) =
        catch({ handle(request)}) { t ->
            Error.InternalServerError(t.message.orEmpty())
                .left()
        }
        .toResponse()

    private fun handle(request: Request): Either<Error, Snippet> =
        request
            .snippetId
            .flatMap { id ->
                repository
                    .get(id)
                    .mapLeft { Error.from(it) }
            }

    private fun Either<Error, Snippet>.toResponse() = fold(
        ifLeft = { error ->
            Response(error.status)
                .contentType(ContentType.APPLICATION_JSON)
                .body(moshi.asFormatString(error))
         },
        ifRight = { snippet ->
            Response(OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(moshi.asFormatString(snippet))
        }
    )

    private val Request.snippetId: Either<Error, UUID>
        get() = either {
            val raw = ensureNotNull(path(PathParam.SNIPPET_ID)) {
                Error.MissingPathParam(PathParam.SNIPPET_ID)
            }

            val uuid = catch({ UUID.fromString(raw) }) { _ ->
                raise(Error.InvalidSnippetId(raw))
            }

            return uuid.right()
        }

    @JsonSerializable
    @Polymorphic("error")
    internal sealed class Error(
        open val status: Status,
        open val message: String
    ) {
        @JsonSerializable
        @PolymorphicLabel("snippet_not_found")
        data class NotFound(
            @Json(ignore = true)
            val id: String = "",
            override val status: Status = NOT_FOUND,
            override val message: String = "Snippet not found with id: $id"
        ) : Error(status, message)

        @JsonSerializable
        @PolymorphicLabel("internal_server_error")
        data class InternalServerError(
            override val message: String,
            override val status: Status = INTERNAL_SERVER_ERROR,
        ) : Error(status, message)

        @JsonSerializable
        @PolymorphicLabel("missing_path_param")
        data class MissingPathParam(
            @Json(ignore = true)
            val param: String? = "",
            override val status: Status = BAD_REQUEST,
            override val message: String = "Missing path parameter: $param"
        ) : Error(status, message)

        @JsonSerializable
        @PolymorphicLabel("invalid_snippet_id")
        data class InvalidSnippetId(
            @Json(ignore = true)
            val id: String = "",
            override val status: Status = BAD_REQUEST,
            override val message: String = "Invalid snippet ID: $id"
        ) : Error(status, message)

        companion object {
            fun from(error: SnippetRepository.Error.GetItemError) = when (error) {
                is SnippetRepository.Error.GetItemError.Failure -> InternalServerError(error.message)
                is SnippetRepository.Error.GetItemError.NotFound -> NotFound(error.id.toString())
            }
        }
    }
}


