import java.math.BigInteger
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

/** 응답의 "result" 값(16진수)을 숫자로 */
fun rpcNumber(response: String): BigInteger = rpcField(response, "result")

/** 응답 JSON에서 이름이 field인 16진수 값을 숫자로. 예: baseFeePerGas, status, blockNumber */
fun rpcField(response: String, field: String): BigInteger {
    val hex = response.substringAfter("\"$field\":\"").substringBefore("\"").removePrefix("0x")
    return BigInteger(hex, 16)
}