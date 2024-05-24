package repository

import arrow.core.Either
import domain.Snippet
import java.util.UUID

/**
 * [Snippet] api.getRepository.
 *
 * Provides methods for storing/retrieving [Snippet]s.
 */
interface SnippetRepository {
    /**
     * Inserts a [Snippet] into the api.getRepository.
     */
    fun insert(snippet: Snippet): Either<Error.InsertItemError, Unit>

    /**
     * Retrieves a [Snippet] by its ID.
     */
    fun get(id: UUID): Either<Error.GetItemError, Snippet>

    /**
     * [SnippetRepository] errors.
     */
    sealed class Error(open val message: String) {
        /**
         * Errors that can result from calling [SnippetRepository.insert].
         */
        sealed class InsertItemError(message: String) : Error(message) {
            /**
             * Unexpected failure.
             */
            data class Failure(override val message: String) : InsertItemError(message)
        }

        /**
         * Errors that can result from calling [SnippetRepository.get].
         */
        sealed class GetItemError(message: String) : Error(message) {
            /**
             * Snippet not found.
             *
             * @param id ID of the snippet that was not found.
             */
            data class NotFound(val id: UUID) : GetItemError("Snippet with id '$id' not found")

            /**
             * Unexpected failure.
             */
            data class Failure(override val message: String) : GetItemError(message)
        }
    }
}
