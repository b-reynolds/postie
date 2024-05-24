package api.configuration

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.http4k.client.JavaHttpClient
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.DynamoDbJsonAdapterFactory
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.model.AttributeName
import org.http4k.connect.amazon.dynamodb.model.ClientToken
import org.http4k.connect.amazon.dynamodb.model.IndexName
import org.http4k.connect.amazon.dynamodb.model.NextToken
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.format.AwsCoreJsonAdapterFactory
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withAwsCoreMappings
import org.http4k.format.withStandardMappings
import repository.DynamoDbSnippetRepository
import repository.SnippetRepository
import se.ansman.kotshi.KotshiJsonAdapterFactory

val moshi by lazy {
    ConfigurableMoshi(
        Moshi.Builder()
            .add(JsonAdapterFactory)
            .add(DynamoDbJsonAdapterFactory)
            .add(AwsCoreJsonAdapterFactory())
            .add(MapAdapter)
            .add(ListAdapter)
            .asConfigurable()
            .withStandardMappings()
            .withAwsCoreMappings()
            .value(AttributeName)
            .value(IndexName)
            .value(TableName)
            .value(ClientToken)
            .value(NextToken)
            .done()
    )
}

val repository: SnippetRepository by lazy {
    DynamoDbSnippetRepository(
        client = DynamoDb.Http(http = JavaHttpClient()),
        tableName = TableName.of("snippets"),
        autoMarshalling = moshi
    )
}

@KotshiJsonAdapterFactory
private object JsonAdapterFactory : JsonAdapter.Factory by KotshiJsonAdapterFactory
