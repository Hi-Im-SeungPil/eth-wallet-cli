import org.bouncycastle.crypto.ec.CustomNamedCurves
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

    val env = loadEnv()
    val address = addressCalculator(env)

    val balanceResponse = rpcCall(env.getValue("SEPOLIA_RPC_URL"), "eth_getBalance", """["$address","latest"]""")
    val balance = balanceResponse.substringAfter("\"result\":\"").substringBefore("\"").removePrefix("0x")
    val balanceAdoptWei = BigInteger(balance, 16).toBigDecimal().movePointLeft(18)
    println(address)
    println(balanceAdoptWei.stripTrailingZeros().toPlainString())
}