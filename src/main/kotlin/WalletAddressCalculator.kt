import java.math.BigInteger

fun addressCalculator(env: Map<String, String>): String {
    val privateKey = BigInteger(env.getValue("PRIVATE_KEY").removePrefix("0x"), 16)
    return addressFromPrivateKey(privateKey)
}