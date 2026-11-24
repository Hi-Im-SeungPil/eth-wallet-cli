import org.bouncycastle.crypto.ec.CustomNamedCurves

fun main() {
    val curve = CustomNamedCurves.getByName("secp256k1")
    println("eth-wallet-cli 준비 완료")
    println("secp256k1 곡선 로드: ${curve.curve.fieldSize}비트")

    val key = generatePrivateKey()
    println("개인키: 0x${key.toString(16).padStart(64, '0')}")
    println("주소:   ${addressFromPrivateKey(key)}")
}