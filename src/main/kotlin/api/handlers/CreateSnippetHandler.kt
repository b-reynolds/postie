package api.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.ConfigurableMoshi
import repository.SnippetRepository

class CreateSnippetHandler(
    private val repository: SnippetRepository,
    private val moshi: ConfigurableMoshi
) : HttpHandler {
    override fun invoke(request: Request): Response = Response(Status.OK)
}
