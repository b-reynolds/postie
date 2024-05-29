package api.configuration

import api.configuration.serialization.moshi
import api.validation.SnippetValidator
import org.http4k.client.JavaHttpClient
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.model.TableName
import repository.DynamoDbSnippetRepository
import repository.SnippetRepository

val repository: SnippetRepository by lazy {
    DynamoDbSnippetRepository(
        client = DynamoDb.Http(http = JavaHttpClient()),
        tableName = TableName.of("snippets"),
        autoMarshalling = moshi
    )
}

val snippetValidator by lazy {
    SnippetValidator(100_000)
}



