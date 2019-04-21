import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import server.startServer


private fun request(method: String, url: String): Pair<Int, String> {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = method
    if (conn.responseCode > 299) {
        return Pair(conn.responseCode, InputStreamReader(conn.errorStream).readText())
    } else {
        return Pair(conn.responseCode, InputStreamReader(conn.inputStream).readText())
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestsAPI {

    init {
        startServer()
    }

    @Test
    fun testCreateAndGetAccount() {
        val (code1, acc) = request(
            "POST",
            "http://localhost:8080/v1/accounts/?amount=3.141592653589793238462643383279502884197169")
        Assertions.assertEquals(201, code1)

        val (code2, amount) = request("GET", "http://localhost:8080/v1/accounts/$acc")
        Assertions.assertEquals(200, code2)
        Assertions.assertEquals("3.141592653589793238462643383279502884197169", amount)

        val (code3, _) = request("GET", "http://localhost:8080/v1/accounts/123-456-dsds")
        Assertions.assertEquals(404, code3)
    }

    @Test
    fun testTransferMoney() {
        val (_, sender) = request(
            "POST",
            "http://localhost:8080/v1/accounts/?amount=3.141592653589793238462643383279502884197169")
        val (_, receiver) = request("POST", "http://localhost:8080/v1/accounts/?amount=0")

        val (code1, _) = request(
            "POST",
            "http://localhost:8080/v1/transfers/?sender=$sender&receiver=$receiver&amount=1.000000000000000000000000000000000000000001")
        Assertions.assertEquals(200, code1)

        val (_, amount1) = request("GET", "http://localhost:8080/v1/accounts/$sender")
        Assertions.assertEquals("2.141592653589793238462643383279502884197168", amount1)

        val (_, amount2) = request("GET", "http://localhost:8080/v1/accounts/$receiver")
        Assertions.assertEquals("1.000000000000000000000000000000000000000001", amount2)

        val (code2, _) = request(
            "POST",
            "http://localhost:8080/v1/transfers/?sender=123-456&receiver=$receiver&amount=5")
        Assertions.assertEquals(404, code2)

        val (code3, _) = request(
            "POST",
            "http://localhost:8080/v1/transfers/?sender=$sender&receiver=123-456&amount=5")
        Assertions.assertEquals(404, code3)
    }

    @Test
    fun testSomeMalformedRequests() {
        val(code1, _) = request("POST", "http://localhost:8080/v1/accounts/123/?amount=10")
        Assertions.assertEquals(405, code1)

        val(code2, _) = request("GET", "http://localhost:8080/v1/accounts/")
        Assertions.assertEquals(405, code2)

        val(code3, _) = request("POST", "http://localhost:8080/v1/accounts/")
        Assertions.assertEquals(400, code3)

        val (code4, _) = request("POST", "http://localhost:8080/v1/accounts/?amount=10a")
        Assertions.assertEquals(400, code4)

        val (code5, _) = request("POST", "http://localhost:8080/v1/accounts/?amount=10&amount=1")
        Assertions.assertEquals(400, code5)

        val (code6, _) = request("POST", "http://localhost:8080/v1/accounts/?amount=10&a=1")
        Assertions.assertEquals(400, code6)

        val (code7, _) = request("GET", "http://localhost:8080/v1/accounts/123/?a=1")
        Assertions.assertEquals(400, code7)

        val (code8, _) = request("GET", "http://localhost:8080/v1/accounts/")
        Assertions.assertEquals(405, code8)

        val (code9, _) = request("GET", "http://localhost:8080/v1/transfers/")
        Assertions.assertEquals(405, code9)

        val (code10, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456")
        Assertions.assertEquals(400, code10)

        val (code11, _) = request("POST", "http://localhost:8080/v1/transfers/123?sender=123&receiver=456")
        Assertions.assertEquals(404, code11)

        val (code12, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456&amount=1a")
        Assertions.assertEquals(400, code12)

        val (code13, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&amount=1")
        Assertions.assertEquals(400, code13)

        val (code14, _) = request("POST", "http://localhost:8080/v1/transfers/?receiver=123&amount=1")
        Assertions.assertEquals(400, code14)

        val (code15, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456&amount=1&sender=789")
        Assertions.assertEquals(400, code15)

        val (code16, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456&amount=1&receiver=789")
        Assertions.assertEquals(400, code16)

        val (code17, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456&amount=1&amount=1")
        Assertions.assertEquals(400, code17)

        val (code18, _) = request("POST", "http://localhost:8080/v1/transfers/?sender=123&receiver=456&amount=1&a=1")
        Assertions.assertEquals(400, code18)
    }
}
