import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

/** 이더리움 공식 RLP 문서의 예시값으로 검증 */
class RlpTest {

    private fun hex(bytes: ByteArray) = bytes.toHexString()

    // 규칙 1: 0x00~0x7f 바이트 하나는 그대로
    @Test
    fun `작은 바이트 하나는 그대로`() {
        assertEquals("0f", hex(rlpEncodeBytes(byteArrayOf(0x0f))))
        assertEquals("7f", hex(rlpEncodeBytes(byteArrayOf(0x7f))))
    }

    // 규칙 2: 0~55바이트 문자열
    @Test
    fun `짧은 바이트열은 0x80 + 길이`() {
        assertEquals("83646f67", hex(rlpEncodeBytes("dog".toByteArray())))
        assertEquals("80", hex(rlpEncodeBytes(ByteArray(0))))
        assertEquals("8180", hex(rlpEncodeBytes(byteArrayOf(0x80.toByte()))))
    }

    // 규칙 3: 56바이트 이상 문자열
    @Test
    fun `긴 바이트열은 0xb7 + 길이의 길이`() {
        val text = "Lorem ipsum dolor sit amet, consectetur adipisicing elit" // 56바이트
        assertEquals("b838" + hex(text.toByteArray()), hex(rlpEncodeBytes(text.toByteArray())))
    }

    // 규칙 4: 리스트
    @Test
    fun `리스트는 0xc0 + 내용물 길이`() {
        val catDog = listOf(rlpEncodeBytes("cat".toByteArray()), rlpEncodeBytes("dog".toByteArray()))
        assertEquals("c88363617483646f67", hex(rlpEncodeList(catDog)))
        assertEquals("c0", hex(rlpEncodeList(emptyList())))
    }

    // 숫자
    @Test
    fun `숫자는 앞자리 0 없는 바이트열로`() {
        assertEquals("80", hex(rlpEncodeBigInteger(BigInteger.ZERO)))
        assertEquals("0f", hex(rlpEncodeBigInteger(BigInteger.valueOf(15))))
        assertEquals("8180", hex(rlpEncodeBigInteger(BigInteger.valueOf(128))))
        assertEquals("820400", hex(rlpEncodeBigInteger(BigInteger.valueOf(1024))))
    }
}
