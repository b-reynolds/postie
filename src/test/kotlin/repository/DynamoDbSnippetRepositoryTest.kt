package repository

import domain.Snippet
import moshi
import org.http4k.aws.AwsCredentials
import org.http4k.connect.amazon.core.model.Region
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.filter.debug
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.arrow.isLeft
import strikt.arrow.isRight
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.time.Instant
import java.util.UUID

class DynamoDbSnippetRepositoryTest {
    @Test
    fun `insert() with valid DynamoDB configuration succeeds`() {
        expectThat(
            DynamoDbSnippetRepository(dynamoDb, TableName.of(TABLE_NAME), moshi)
                .insert(snippet)
        )
            .isRight()
    }

    @Test
    fun `insert() with invalid DynamoDB configuration (missing required table) fails`() {
        expectThat(
            DynamoDbSnippetRepository(dynamoDbWithoutTable, TableName.of(TABLE_NAME), moshi)
                .insert(snippet)
        )
            .isLeft()
    }

    @Test
    fun `get() with Snippet ID present in DynamoDB returns snippet`() {
        expectThat(
            DynamoDbSnippetRepository(dynamoDb, TableName.of(TABLE_NAME), moshi)
                .apply { insert(snippet) }
                .get(snippet.id)
        )
            .isRight()
            .get { value }
            .isEqualTo(snippet)
    }

    @Test
    fun `get() with valid invalid DynamoDB configuration (missing required table) results in GetItemError#Failure`() {
        expectThat(
            DynamoDbSnippetRepository(dynamoDbWithoutTable, TableName.of(TABLE_NAME), moshi)
                .get(snippet.id)
        )
            .isLeft()
            .get { leftOrNull() }
            .isA<SnippetRepository.Error.GetItemError.Failure>()
    }

    @Test
    fun `get() with Snippet ID not present in DynamoDB results in GetItemError#NotFound`() {
        expectThat(
            DynamoDbSnippetRepository(dynamoDb, TableName.of(TABLE_NAME), moshi)
                .get(snippet.id)
        )
            .isLeft()
            .get { leftOrNull() }
            .isA<SnippetRepository.Error.GetItemError.NotFound>()
            .get { id }
            .isEqualTo(snippet.id)
    }
}

private val snippet = Snippet(
    id = UUID.randomUUID().toString(),
    title = "title",
    contents = "contents",
    format = "txt",
    createdAt = Instant.now(),
    expiresAt = Instant.now()
)

private val dynamoDb get() = DynamoDb.Http(
    region = Region.EU_WEST_2,
    credentialsProvider = { AwsCredentials("id", "secret") },
    http = FakeDynamoDb().debug()
)
    .apply {
        tableMapper<Snippet, String, Unit>(
            tableName = TableName.of(TABLE_NAME),
            hashKeyAttribute = Attribute.string().required(Snippet.ID)
        )
        .createTable()
    }

private val dynamoDbWithoutTable get() = DynamoDb.Http(
    region = Region.EU_WEST_2,
    credentialsProvider = { AwsCredentials("id", "secret") },
    http = FakeDynamoDb().debug()
)

private const val TABLE_NAME = "snippets"
