import java.math.BigDecimal
import java.math.BigInteger

private val ADDRESS_FORMAT = Regex("0x[0-9a-fA-F]{40}")
private val SIMPLE_TRANSFER_GAS = BigInteger.valueOf(21000)

/** D1~D4를 한 번에: 값 모으기 → 요청서 만들기 → 서명 → 전송 → 블록 포함 확인 */
fun sendEth(env: Map<String, String>, to: String, amountEth: BigDecimal) {
    // 주소가 틀리면 그 주소로 간 돈은 아무도 못 꺼냄. 형식부터 확인
    require(to.matches(ADDRESS_FORMAT)) { "받는 주소 형식이 아니에요: $to" }

    val rpcUrl = env.getValue("SEPOLIA_RPC_URL")
    val privateKey = BigInteger(env.getValue("PRIVATE_KEY").removePrefix("0x"), 16)
    val from = addressFromPrivateKey(privateKey)

    // D1: 요청서에 들어갈 값 모으기
    val chainId = rpcNumber(rpcCall(rpcUrl, "eth_chainId"))
    val nonce = rpcNumber(rpcCall(rpcUrl, "eth_getTransactionCount", """["$from","pending"]"""))
    val tip = rpcNumber(rpcCall(rpcUrl, "eth_maxPriorityFeePerGas"))
    val baseFee = rpcField(rpcCall(rpcUrl, "eth_getBlockByNumber", """["latest",false]"""), "baseFeePerGas")
    val maxFee = baseFee * BigInteger.TWO + tip

    val tx = Eip1559Transaction(
        chainId = chainId,
        nonce = nonce,
        maxPriorityFeePerGas = tip,
        maxFeePerGas = maxFee,
        gasLimit = SIMPLE_TRANSFER_GAS,
        to = to,
        value = amountEth.movePointRight(18).toBigIntegerExact(),
    )
    println("보내는 주소: $from")
    println("받는 주소:   $to")
    println("금액: $amountEth ETH, nonce: $nonce, 팁: $tip wei, 최대 수수료: $maxFee wei")

    // D2, D3: RLP 포장 + 서명
    val signed = tx.sign(privateKey)
    val ourHash = "0x" + signed.hash.toHexString()

    // D4: 노드에 전송
    val response = rpcCall(rpcUrl, "eth_sendRawTransaction", """["0x${signed.raw.toHexString()}"]""")
    if (response.contains("\"error\"")) {
        println("전송 실패: $response")
        return
    }
    val nodeHash = response.substringAfter("\"result\":\"").substringBefore("\"")
    println("노드가 돌려준 해시: $nodeHash")
    println("우리가 계산한 해시: $ourHash ${if (nodeHash == ourHash) "(일치)" else "(불일치!)"}")
    println("Etherscan: https://sepolia.etherscan.io/tx/$nodeHash")

    // 블록에 들어갈 때까지 3초마다 영수증 확인 (최대 60초)
    repeat(20) { attempt ->
        Thread.sleep(3000)
        val receipt = rpcCall(rpcUrl, "eth_getTransactionReceipt", """["$nodeHash"]""")
        if (!receipt.contains("\"result\":null")) {
            val status = rpcField(receipt, "status")
            val block = rpcField(receipt, "blockNumber")
            println(if (status == BigInteger.ONE) "성공! 블록 $block 에 포함됐어요" else "블록에 들어갔지만 실행 실패 (status 0)")
            return
        }
        println("블록에 들어가길 기다리는 중... (${(attempt + 1) * 3}초)")
    }
    println("60초 안에 확인되지 않았어요. 위 Etherscan 링크에서 상태를 확인하세요")
}
