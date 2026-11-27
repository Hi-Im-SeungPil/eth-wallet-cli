import java.math.BigDecimal

/**
 * 인자 없이 실행: 내 주소와 잔액 출력
 * 송금:          gradlew run --args="받는주소 금액(ETH)"
 */
fun main(args: Array<String>) {
    val env = loadEnv()

    if (args.isEmpty()) {
        val address = addressCalculator(env)
        val balanceResponse = rpcCall(env.getValue("SEPOLIA_RPC_URL"), "eth_getBalance", """["$address","latest"]""")
        val eth = BigDecimal(rpcNumber(balanceResponse)).movePointLeft(18)
        println("내 주소: $address")
        println("잔액: ${eth.stripTrailingZeros().toPlainString()} ETH")
        return
    }

    val to = args[0]
    val amount = BigDecimal(args.getOrElse(1) { "0.001" })
    sendEth(env, to, amount)
}
