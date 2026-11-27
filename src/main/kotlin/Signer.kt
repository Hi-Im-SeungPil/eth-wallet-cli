import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

/** 서명 = (r, s) 두 숫자 + 공개키 복원에 쓰는 yParity(0 또는 1) */
data class Signature(val r: BigInteger, val s: BigInteger, val yParity: Int)

private val CURVE = CustomNamedCurves.getByName("secp256k1")
private val DOMAIN = ECDomainParameters(CURVE.curve, CURVE.g, CURVE.n, CURVE.h)
private val HALF_N = CURVE.n.shiftRight(1)

fun signHash(hash: ByteArray, privateKey: BigInteger): Signature {
    // 1. ECDSA 서명. 일회용 랜덤값 k를 난수 대신 키와 해시로 정해서(RFC 6979) k 재사용 사고를 막음
    val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
    signer.init(true, ECPrivateKeyParameters(privateKey, DOMAIN))
    val (r, rawS) = signer.generateSignature(hash)

    // 2. low-s 규칙(EIP-2): s가 n/2보다 크면 n - s로 바꿈. (r, s)와 (r, n-s)가 둘 다 유효해서 생기는 변조를 막음
    val s = if (rawS > HALF_N) CURVE.n - rawS else rawS

    // 3. yParity: 0과 1 중 어느 쪽으로 복원해야 내 공개키가 나오는지 찾음
    val myPublicKey = CURVE.g.multiply(privateKey).normalize()
    for (yParity in 0..1) {
        if (recoverPublicKey(hash, r, s, yParity) == myPublicKey) {
            return Signature(r, s, yParity)
        }
    }
    error("서명에서 공개키를 복원하지 못함")
}

/** 해시와 서명만으로 공개키를 복원. 노드가 트랜잭션의 보낸 사람(from)을 알아내는 방법 */
fun recoverPublicKey(hash: ByteArray, r: BigInteger, s: BigInteger, yParity: Int): ECPoint? {
    val n = CURVE.n

    // 서명할 때 쓴 점 R: x좌표는 r, y좌표의 홀짝은 yParity → 압축 공개키 형식(02/03 + x)으로 복원
    val compressedR = byteArrayOf((0x02 + yParity).toByte()) + r.toFixed32Bytes()
    val pointR = try {
        CURVE.curve.decodePoint(compressedR)
    } catch (e: IllegalArgumentException) {
        return null
    }

    // 공개키 Q = r⁻¹ · (s·R − z·G)
    val z = BigInteger(1, hash)
    val rInv = r.modInverse(n)
    val q = ECAlgorithms.sumOfTwoMultiplies(
        CURVE.g, z.negate().multiply(rInv).mod(n),
        pointR, s.multiply(rInv).mod(n),
    )
    return q.normalize()
}

/** 앞자리를 0으로 채워 항상 32바이트로 */
private fun BigInteger.toFixed32Bytes(): ByteArray {
    val bytes = toByteArray().dropWhile { it == 0.toByte() }.toByteArray()
    return ByteArray(32 - bytes.size) + bytes
}
