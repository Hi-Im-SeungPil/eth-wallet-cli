import org.bouncycastle.crypto.ec.CustomNamedCurves
import java.math.BigInteger
import java.security.SecureRandom

fun generatePrivateKey(): BigInteger {
    // 1. secp256k1 규칙서에서 n 꺼내기
    // 2. SecureRandom으로 256비트 숫자 뽑기
    // 3. 0보다 크고 n보다 작으면 반환, 아니면 다시 뽑기

    val curve = CustomNamedCurves.getByName("secp256k1")
    val random = SecureRandom()

    val n = curve.n
    var random256 = BigInteger(256, random)

    while (random256 <= BigInteger.ZERO || random256 >= n) {
        random256 = BigInteger(256, random)
    }

    return random256
}