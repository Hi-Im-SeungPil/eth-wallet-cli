import org.bouncycastle.jcajce.provider.digest.Keccak
import java.math.BigInteger

/** EIP-1559(타입 2) 송금 요청서. 숫자는 전부 wei 단위 */
data class Eip1559Transaction(
    val chainId: BigInteger,
    val nonce: BigInteger,
    val maxPriorityFeePerGas: BigInteger,
    val maxFeePerGas: BigInteger,
    val gasLimit: BigInteger,
    val to: String,
    val value: BigInteger,
)

/** 서명이 끝난 요청서. raw는 노드에 보낼 바이트, hash는 Etherscan에 나오는 트랜잭션 해시 */
class SignedTransaction(val raw: ByteArray) {
    val hash: ByteArray = keccak256(raw)
}

/** EIP-1559 트랜잭션은 RLP 앞에 타입 번호 0x02를 붙임 */
private const val TX_TYPE_EIP1559: Byte = 0x02

/** 요청서 항목 9개를 정해진 순서대로 RLP 포장 */
private fun Eip1559Transaction.encodedFields(): List<ByteArray> = listOf(
    rlpEncodeBigInteger(chainId),
    rlpEncodeBigInteger(nonce),
    rlpEncodeBigInteger(maxPriorityFeePerGas),
    rlpEncodeBigInteger(maxFeePerGas),
    rlpEncodeBigInteger(gasLimit),
    rlpEncodeBytes(hexToBytes(to)),
    rlpEncodeBigInteger(value),
    rlpEncodeBytes(ByteArray(0)),   // data: 단순 송금이라 비어 있음
    rlpEncodeList(emptyList()),     // accessList: 비어 있음
)

/** 서명할 대상: 0x02 + RLP(항목 9개) */
fun Eip1559Transaction.signingPayload(): ByteArray =
    byteArrayOf(TX_TYPE_EIP1559) + rlpEncodeList(encodedFields())

/** 서명해서 노드에 보낼 수 있는 형태로: 0x02 + RLP(항목 9개 + yParity, r, s) */
fun Eip1559Transaction.sign(privateKey: BigInteger): SignedTransaction {
    val signature = signHash(keccak256(signingPayload()), privateKey)
    val signedFields = encodedFields() + listOf(
        rlpEncodeBigInteger(BigInteger.valueOf(signature.yParity.toLong())),
        rlpEncodeBigInteger(signature.r),
        rlpEncodeBigInteger(signature.s),
    )
    return SignedTransaction(byteArrayOf(TX_TYPE_EIP1559) + rlpEncodeList(signedFields))
}

fun keccak256(bytes: ByteArray): ByteArray = Keccak.Digest256().digest(bytes)

fun hexToBytes(hex: String): ByteArray = hex.removePrefix("0x").hexToByteArray()
