import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionTest {

    // 테스트 전용 공개 예시 키 (실제 지갑 키가 아님)
    private val privateKey = BigInteger("4c0883a69102937d6231471b5dbb6204fe5129617082792ae468d01a3f362318", 16)
    private val to = "0x95a13f457c76d10a40d7e8497ed4f40c53f4d04b"

    private val tx = Eip1559Transaction(
        chainId = BigInteger.valueOf(11155111),
        nonce = BigInteger.ZERO,
        maxPriorityFeePerGas = BigInteger.valueOf(2),
        maxFeePerGas = BigInteger.valueOf(36),
        gasLimit = BigInteger.valueOf(21000),
        to = to,
        value = BigInteger.TEN.pow(15), // 0.001 ETH
    )

    @Test
    fun `서명한 트랜잭션이 web3j 결과와 바이트 단위로 같다`() {
        val ours = tx.sign(privateKey).raw.toHexString()

        val web3jTx = RawTransaction.createEtherTransaction(
            11155111L, BigInteger.ZERO, BigInteger.valueOf(21000), to,
            BigInteger.TEN.pow(15), BigInteger.valueOf(2), BigInteger.valueOf(36),
        )
        val theirs = TransactionEncoder.signMessage(web3jTx, Credentials.create(ECKeyPair.create(privateKey))).toHexString()

        assertEquals(theirs, ours)
    }

    @Test
    fun `서명에서 복원한 공개키가 내 공개키와 같다`() {
        val hash = keccak256(tx.signingPayload())
        val signature = signHash(hash, privateKey)

        val recovered = recoverPublicKey(hash, signature.r, signature.s, signature.yParity)
        val expected = CustomNamedCurves.getByName("secp256k1").g.multiply(privateKey).normalize()

        assertEquals(expected, recovered)
    }
}
