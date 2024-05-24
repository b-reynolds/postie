package api

import api.configuration.moshi
import api.configuration.repository
import api.handlers.GetSnippetHandler
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.serverless.ApiGatewayV2LambdaFunction

@Suppress("unused")
class Routing(
    private val getSnippetHandler: HttpHandler = GetSnippetHandler(repository, moshi)
) : ApiGatewayV2LambdaFunction(
    routes(
        Path.GET_SNIPPET bind Method.GET to getSnippetHandler::invoke
    )
) {
    object Path {
        const val GET_SNIPPET = "/snippets/{${PathParam.SNIPPET_ID}}"
    }
}

