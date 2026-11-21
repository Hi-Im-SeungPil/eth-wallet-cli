import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.math.BigInteger

/**
 * 2단계 실습: 개인키로 이더리움 주소를 계산한다.
 *
 * 1. 개인키 × G = 공개키 (곡선 위의 점)
 * 2. 압축하지 않은 공개키 65바이트에서 맨 앞 0x04를 뺀 64바이트를 준비
 * 3. 그 64바이트를 Keccak-256으로 해시 (Java의 SHA3-256과 다름!)
 * 4. 해시의 마지막 20바이트를 16진수로 바꾸고 앞에 "0x"를 붙임
 *
 * 다 만들면 AddressTest의 @Ignore를 지우고 테스트를 통과시켜 보세요.
 */
fun addressFromPrivateKey(privateKey: BigInteger): String {
    val curve = CustomNamedCurves.getByName("secp256k1")
    val publicKey = curve.g.multiply(privateKey).normalize()
    val encoded = publicKey.getEncoded(false)
    val pubKeyBytes = encoded.copyOfRange(1, 65)
    val keccakBytes = Keccak.Digest256().digest(pubKeyBytes) // 32바이트
    return "0x${keccakBytes.copyOfRange(12, 32).toHexString()}"
}
