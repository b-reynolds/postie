package repository

import arrow.core.Either
import domain.Snippet

/**
 * [Snippet] repository.
 *
 * Provides methods for storing/retrieving [Snippet]s.
 */
interface SnippetRepository {
    /**
     * Inserts a [Snippet] into the repository.
     */
    fun insert(snippet: Snippet): Either<Error.InsertItemError, Unit>

    /**
     * Retrieves a [Snippet] by its ID.
     */
    fun get(id: String): Either<Error.GetItemError, Snippet>

    /**
     * [SnippetRepository] errors.
     */
    sealed class Error(val message: String) {
        /**
         * Errors that can result from calling [SnippetRepository.insert].
         */
        sealed class InsertItemError(message: String) : Error(message) {
            /**
             * Unexpected failure.
             */
            class Failure(message: String) : InsertItemError(message)
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
            class NotFound(val id: String) : GetItemError("Snippet with id '$id' not found")

            /**
             * Unexpected failure.
             */
            class Failure(message: String) : GetItemError(message)
        }
    }
}



