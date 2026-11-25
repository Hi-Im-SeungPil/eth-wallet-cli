import java.math.BigInteger

fun main() {
//    val curve = CustomNamedCurves.getByName("secp256k1")
//    println("eth-wallet-cli 준비 완료")
//    println("secp256k1 곡선 로드: ${curve.curve.fieldSize}비트")
//
//    val key = generatePrivateKey()
//    println("개인키: 0x${key.toString(16).padStart(64, '0')}")
//    println("주소:   ${addressFromPrivateKey(key)}")

//    val env = loadEnv()
//    val response = rpcCall(env.getValue("SEPOLIA_RPC_URL"), "eth_blockNumber")
//    val hex = response.substringAfter("\"result\":\"").substringBefore("\"")
//    val realHex = BigInteger(hex.removePrefix("0x"), 16)
//    val ten = BigInteger(realHex.toString(), 10)

//    val env = loadEnv()
//    val address = addressCalculator(env)
//
//    val balanceResponse = rpcCall(env.getValue("SEPOLIA_RPC_URL"), "eth_getBalance", """["$address","latest"]""")
//    val balance = balanceResponse.substringAfter("\"result\":\"").substringBefore("\"").removePrefix("0x")
//    val balanceAdoptWei = BigInteger(balance, 16).toBigDecimal().movePointLeft(18)
//    println(address)
//    println(balanceAdoptWei.stripTrailingZeros().toPlainString())

    val env = loadEnv()
    val address = addressCalculator(env)
    val sepoliaUrl = env.getValue("SEPOLIA_RPC_URL")
    val chainID = rpcCall(sepoliaUrl, "eth_chainId")
    val nonce = rpcCall(sepoliaUrl, "eth_getTransactionCount", """["$address","pending"]""")
    val tip = rpcCall(sepoliaUrl, "eth_maxPriorityFeePerGas")
    val baseFeeRes = rpcCall(sepoliaUrl, "eth_getBlockByNumber", """["latest",false]""")
    val baseFeeFeeResult = baseFeeRes.substringAfter("\"baseFeePerGas\":\"")
        .substringBefore("\"")
        .removePrefix("0x")
    val baseFee = BigInteger(baseFeeFeeResult, 16)

    println("chainId -> ${rpcNumber(chainID)}")
    println("nonce -> ${rpcNumber(nonce)}")
    println("tip -> ${rpcNumber(tip)}")
    println("baseFee -> $baseFee")
    println("maxFee -> ${baseFee.multiply(BigInteger.TWO).add(rpcNumber(tip))}")
}

fun rpcNumber(response: String): BigInteger {
    // "result" 값 꺼내기 → 0x 떼고 16진수로 읽어 반환
    val result = response.substringAfter("\"result\":\"").substringBefore("\"").removePrefix("0x")
    return BigInteger(result, 16)
}