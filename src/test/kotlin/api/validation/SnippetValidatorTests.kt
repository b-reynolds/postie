package api.validation

import domain.Snippet
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.arrow.isLeft
import strikt.arrow.isRight
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SnippetValidatorTests {
    private val validator = SnippetValidator(MAX_CONTENTS_LENGTH)

    @Test
    fun `Valid snippet is returned`() {
        expectThat(validator.validate(snippet))
            .isRight()
            .get { value }
            .isEqualTo(snippet)
    }

    @Test
    fun `Snippet with empty contents returns ContentsCantBeEmpty`() {
        val snippetWithEmptyContents = snippet.copy(contents = "")

        expectThat(validator.validate(snippetWithEmptyContents))
            .isLeft()
            .get { value }
            .containsExactly(SnippetValidator.SnippetValidationError.ContentsCantBeEmpty())
    }

    @Test
    fun `Snippet with contents too large returns ContentsTooLarge`() {
        val snippetWithEmptyContents = snippet.copy(contents = "contentstoolarge")

        expectThat(validator.validate(snippetWithEmptyContents))
            .isLeft()
            .get { value }
            .containsExactly(SnippetValidator.SnippetValidationError.ContentsTooLarge(MAX_CONTENTS_LENGTH))
    }

    @Test
    fun `Snippet with expiry date not in the future returns ExpiryDateMustBeFuture`() {
        val snippetWithEmptyContents = snippet.copy(expiresAt = Instant.now().minusSeconds(1))

        expectThat(validator.validate(snippetWithEmptyContents))
            .isLeft()
            .get { value }
            .containsExactly(SnippetValidator.SnippetValidationError.ExpiryDateMustBeFuture())
    }

    @Test
    fun `Snippet validation errors are accumulated`() {
        val snippetWithMultipleErrors = snippet.copy(
            contents = "contentstoolarge",
            expiresAt = Instant.now().minusSeconds(1)
        )

        expectThat(validator.validate(snippetWithMultipleErrors))
            .isLeft()
            .get { value }
            .containsExactly(
                SnippetValidator.SnippetValidationError.ContentsTooLarge(MAX_CONTENTS_LENGTH),
                SnippetValidator.SnippetValidationError.ExpiryDateMustBeFuture()
            )
    }
}

private val snippet = Snippet(
    id = UUID.randomUUID(),
    title = "title",
    contents = "contents",
    createdAt = Instant.now(),
    format = "txt",
    expiresAt = Instant.now().plus(Duration.ofDays(1))
)

private const val MAX_CONTENTS_LENGTH = 16
