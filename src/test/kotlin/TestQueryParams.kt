import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import server.ParseQueryParamsException

import server.QueryParams


class TestsQueryParams {

    @Test
    fun testQueryParamsParser() {
        val params = QueryParams("a=&b=str&c=3.141592653589793238462643383279502884197169")
        Assertions.assertEquals("", params.getString("a"))
        Assertions.assertEquals("str", params.getString("b"))
        Assertions.assertEquals(
            "3.141592653589793238462643383279502884197169",
            params.getBigDecimal("c").toString())
        Assertions.assertThrows(ParseQueryParamsException::class.java) { params.getString("d") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { params.getBigDecimal("b") }
    }

    @Test
    fun testMalfrormedQuery() {
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("a=1&") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("&a=1") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("a=1&&b=1") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("a") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("a==") }
        Assertions.assertThrows(ParseQueryParamsException::class.java) { QueryParams("a=b=c") }
    }

    @Test
    fun testEnsureAllUsed() {
        val params = QueryParams("a=str&b=1&c=1")
        params.getString("a")
        params.getBigDecimal("b")
        Assertions.assertThrows(ParseQueryParamsException::class.java) { params.ensureAllUsed() }
        params.getString("c")
        Assertions.assertDoesNotThrow{ params.ensureAllUsed() }
    }
}