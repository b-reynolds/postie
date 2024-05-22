package repository

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import domain.Snippet
import extensions.toEither
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.getItem
import org.http4k.connect.amazon.dynamodb.model.AttributeName
import org.http4k.connect.amazon.dynamodb.model.AttributeValue
import org.http4k.connect.amazon.dynamodb.model.Item
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.amazon.dynamodb.model.with
import org.http4k.connect.amazon.dynamodb.putItem
import org.http4k.format.AutoMarshalling
import org.http4k.format.autoDynamoLens
import repository.SnippetRepository.Error

/**
 * [SnippetRepository] implementation backed by DynamoDB.
 *
 * @param client [DynamoDb] client that will be used.
 * @param tableName Name of the table that will be used
 * @param autoMarshalling Used to convert [Snippet]s to/from their DynamoDB representation (attribute maps).
 */
class DynamoDbSnippetRepository(
    private val client: DynamoDb,
    private val tableName: TableName,
    autoMarshalling: AutoMarshalling
) : SnippetRepository {
    private val lens = autoMarshalling.autoDynamoLens<Snippet>()

    override fun insert(snippet: Snippet) =
        client
            .putItem(tableName, Item().with(lens of snippet))
            .toEither()
            .mapLeft { failure -> Error.InsertItemError.Failure(failure.message.orEmpty()) }
            .map { }

    override fun get(id: String): Either<Error.GetItemError, Snippet> = either {
        val response = client
            .getItem(tableName, mapOf(AttributeName.of(Snippet.ID) to AttributeValue.Str(id)))
            .toEither()
            .mapLeft { failure -> Error.GetItemError.Failure(failure.message.orEmpty()) }
            .bind()

        val item = response.item ?: raise(Error.GetItemError.NotFound(id))
        catch({ lens(item) }) { t ->
            raise(Error.GetItemError.Failure(t.message.orEmpty()))
        }
    }
}
