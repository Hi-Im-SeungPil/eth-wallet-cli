import java.math.BigInteger

/**
 * RLP(Recursive Length Prefix) 인코딩: 데이터 앞에 "크기 라벨"을 붙여 바이트로 포장하는 이더리움의 규칙
 *
 * 첫 바이트 범위로 종류를 구분함
 * - 0x00~0x7f : 데이터 그 자체 (라벨 없음)
 * - 0x80~0xbf : 바이트열 라벨
 * - 0xc0~0xff : 리스트 라벨
 */
fun rlpEncodeBytes(bytes: ByteArray): ByteArray {
    // 규칙 1: 바이트 하나이고 0x00~0x7f면 라벨 없이 그대로
    // (Kotlin Byte는 -128~127이라 0x80 이상은 음수로 보임 → "0 이상"이면 0x7f 이하)
    if (bytes.size == 1 && bytes[0] >= 0) {
        return bytes
    }
    // 규칙 2, 3: 바이트열 라벨(0x80 기준) + 내용
    return lengthLabel(bytes.size, 0x80) + bytes
}

/** encodedItems는 이미 RLP로 인코딩된 항목들 */
fun rlpEncodeList(encodedItems: List<ByteArray>): ByteArray {
    // 항목들을 순서대로 이어 붙인 것이 리스트의 내용물
    val payload = encodedItems.fold(ByteArray(0)) { acc, item -> acc + item }
    // 규칙 4, 5: 리스트 라벨(0xc0 기준) + 내용물
    return lengthLabel(payload.size, 0xc0) + payload
}

fun rlpEncodeBigInteger(value: BigInteger): ByteArray {
    // 숫자 → 바이트. 앞자리 0x00은 떼야 함 (0은 빈 바이트열이 됨)
    return rlpEncodeBytes(value.toUnsignedBytes())
}

/**
 * 크기 라벨 만들기. offset은 바이트열이면 0x80, 리스트면 0xc0
 * - 길이 0~55   : [offset + 길이]
 * - 길이 56 이상 : [offset + 55 + 길이를 쓰는 데 필요한 바이트 수] + [길이]
 */
private fun lengthLabel(length: Int, offset: Int): ByteArray {
    if (length <= 55) {
        return byteArrayOf((offset + length).toByte())
    }
    val lengthBytes = BigInteger.valueOf(length.toLong()).toUnsignedBytes()
    return byteArrayOf((offset + 55 + lengthBytes.size).toByte()) + lengthBytes
}

/**
 * BigInteger.toByteArray()는 부호 표시용 0x00을 앞에 붙일 때가 있음 (예: 128 → 00 80, 0 → 00)
 * RLP는 앞자리 0을 허용하지 않으므로 떼어 냄
 */
private fun BigInteger.toUnsignedBytes(): ByteArray =
    toByteArray().dropWhile { it == 0.toByte() }.toByteArray()
