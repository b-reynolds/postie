package api

import api.configuration.repository
import api.configuration.serialization.moshi
import api.configuration.snippetValidator
import api.handlers.CreateSnippetHandler
import api.handlers.GetSnippetHandler
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.serverless.ApiGatewayV2LambdaFunction

@Suppress("unused")
class Routing(
    private val getSnippetHandler: HttpHandler = GetSnippetHandler(repository, moshi),
    private val createSnippetHandler: HttpHandler = CreateSnippetHandler(repository, moshi, snippetValidator)
) : ApiGatewayV2LambdaFunction(
    routes(
        Path.GET_SNIPPET bind Method.GET to getSnippetHandler::invoke,
        Path.CREATE_SNIPPET bind Method.POST to createSnippetHandler::invoke
    )
) {
    object Path {
        const val GET_SNIPPET = "/snippets/{${Param.SNIPPET_ID}}"
        const val CREATE_SNIPPET = "/snippets"

        object Param {
            const val SNIPPET_ID = "id"
        }
    }
}

