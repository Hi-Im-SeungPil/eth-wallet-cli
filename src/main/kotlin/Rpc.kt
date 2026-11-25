import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun rpcCall(rpcUrl: String, method: String, params: String = "[]"): String {
    val body = """{"jsonrpc":"2.0","id":1,"method":"$method","params":$params}"""

    val request = HttpRequest.newBuilder(URI.create(rpcUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}