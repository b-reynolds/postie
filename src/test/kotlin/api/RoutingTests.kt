package api

import api.handlers.CreateSnippetHandler
import api.handlers.GetSnippetHandler
import com.amazonaws.services.lambda.runtime.Context
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.HttpHandler
import org.junit.jupiter.api.Test
import java.io.OutputStream

class RoutingTests {
    private val output = mockk<OutputStream>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun `Routes 'Get Snippet' requests to the configured GetSnippetHandler`() {
        val handler : HttpHandler = mockk<GetSnippetHandler>()

        apiGatewayGetRequest(Routing.Path.GET_SNIPPET)
            .byteInputStream()
            .use { request ->
                Routing(getSnippetHandler = handler, mockk())
                    .handleRequest(request, output, context)
            }

        verify { handler.invoke(any()) }
    }

    @Test
    fun `Routes 'Create Snippet' requests to the configured CreateSnippetSnippetHandler`() {
        val handler : HttpHandler = mockk<CreateSnippetHandler>()

        apiGatewayPostRequest(Routing.Path.CREATE_SNIPPET)
            .byteInputStream()
            .use { request ->
                Routing(mockk(), createSnippetHandler = handler)
                    .handleRequest(request, output, context)
            }

        verify { handler.invoke(any()) }
    }
}

private fun apiGatewayGetRequest(path: String) = """
{
  "version": "2.0",
  "routeKey": "GET $path",
  "rawPath": "$path",
  "rawQueryString": "",
  "headers": {
    "accept": "*/*",
    "authorization": "Bearer your-access-token",
    "content-length": "0",
    "host": "your-api-id.execute-api.your-region.amazonaws.com"
  },
  "requestContext": {
    "accountId": "123456789012",
    "apiId": "your-api-id",
    "authorizer": {
      "jwtConfiguration": {
        "issuer": "https://issuer-url",
        "jwtHeader": "Authorization",
        "jwtIssuer": "https://issuer-url"
      }
    },
    "domainName": "your-api-id.execute-api.your-region.amazonaws.com",
    "domainPrefix": "your-api-id",
    "http": {
      "method": "GET",
      "path": "/snippets/123",
      "protocol": "HTTP/1.1",
      "sourceIp": "1.2.3.4",
      "userAgent": "Mozilla/5.0 (...) AppleWebKit/537.36 (...) Chrome/88.0.4324.146 (...) Safari/537.36"
    },
    "requestId": "d2b20c56-71ce-42b8-b00f-e683b81bf11f",
    "routeKey": "GET $path",
    "stage": "your-stage",
    "time": "2022-05-25T12:34:56Z",
    "timeEpoch": 1654042496000
  },
  "isBase64Encoded": false
}
""".trimIndent()

private fun apiGatewayPostRequest(path: String, requestBody: String = "") = """
{
  "version": "2.0",
  "routeKey": "POST $path",
  "rawPath": "$path",
  "rawQueryString": "",
  "headers": {
    "accept": "*/*",
    "authorization": "Bearer your-access-token",
    "content-length": "${requestBody.length}",
    "content-type": "application/json",
    "host": "your-api-id.execute-api.your-region.amazonaws.com"
  },
  "requestContext": {
    "accountId": "123456789012",
    "apiId": "your-api-id",
    "authorizer": {
      "jwtConfiguration": {
        "issuer": "https://issuer-url",
        "jwtHeader": "Authorization",
        "jwtIssuer": "https://issuer-url"
      }
    },
    "domainName": "your-api-id.execute-api.your-region.amazonaws.com",
    "domainPrefix": "your-api-id",
    "http": {
      "method": "POST",
      "path": "$path",
      "protocol": "HTTP/1.1",
      "sourceIp": "1.2.3.4",
      "userAgent": "Mozilla/5.0 (...) AppleWebKit/537.36 (...) Chrome/88.0.4324.146 (...) Safari/537.36"
    },
    "requestId": "d2b20c56-71ce-42b8-b00f-e683b81bf11f",
    "routeKey": "POST $path",
    "stage": "your-stage",
    "time": "2022-05-25T12:34:56Z",
    "timeEpoch": 1654042496000
  },
  "body": "$requestBody",
  "isBase64Encoded": false
}
""".trimIndent()
