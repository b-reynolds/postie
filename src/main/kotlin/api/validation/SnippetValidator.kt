package api.validation

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import domain.Snippet
import java.time.Instant

class SnippetValidator(
    private val maxContentsLength: Int
) {
    fun validate(snippet: Snippet): Either<NonEmptyList<SnippetValidationError>, Snippet> = either {
        zipOrAccumulate(
            {
                ensure(snippet.contents.isNotBlank()) {
                    SnippetValidationError.ContentsCantBeEmpty()
                }
            },
            {
                ensure(snippet.contents.length < maxContentsLength) {
                    SnippetValidationError.ContentsTooLarge(maxContentsLength)
                }
            },
            {
                ensure(snippet.expiresAt == null || snippet.expiresAt.isAfter(Instant.now())) {
                    SnippetValidationError.ExpiryDateMustBeFuture()
                }
            },
        ) { _, _, _ ->
            snippet
        }
    }

    sealed class SnippetValidationError(
        open val description: String,
    ) {
        data class ContentsTooLarge(
            val sizeLimitKb: Int,
            override val description: String = "Contents cannot be larger than $sizeLimitKb characters",
        ) : SnippetValidationError(description)

        data class ContentsCantBeEmpty(
            override val description: String = "Contents cannot be empty"
        ) : SnippetValidationError(description)

        data class ExpiryDateMustBeFuture(
            override val description: String = "Expiry date must be in the future"
        ) : SnippetValidationError(description)
    }
}
