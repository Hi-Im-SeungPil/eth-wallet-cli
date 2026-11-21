import java.math.BigInteger
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressTest {

    @Test
    fun `개인키 1의 주소는 잘 알려진 테스트 벡터와 같다`() {
        val address = addressFromPrivateKey(BigInteger.ONE)

        assertEquals("0x7e5f4552091a69125d5dfcb7b8c2659029395bdf", address)
    }
}
