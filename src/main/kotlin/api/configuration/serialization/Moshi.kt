package api.configuration.serialization

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.http4k.connect.amazon.dynamodb.DynamoDbJsonAdapterFactory
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
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
private object JsonAdapterFactory : JsonAdapter.Factory by KotshiJsonAdapterFactory

val moshi by lazy {
    ConfigurableMoshi(
        Moshi.Builder()
            .add(JsonAdapterFactory)
            .add(DynamoDbJsonAdapterFactory)
            .add(AwsCoreJsonAdapterFactory())
            .add(MapAdapter)
            .add(ListAdapter)
            .add(InstantAdapter())
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
