package server

import com.sun.net.httpserver.*
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.logging.Logger

import accounts.AccountDoesntExistException
import accounts.getAccount
import accounts.createAccount
import accounts.transferMoney


class ParseQueryParamsException : Exception()

class QueryParams(query: String?) {
    private var params = mutableMapOf<String, String>()
    private var usedParams = mutableSetOf<String>()

    init {

        query
            ?.split("&")
            ?.map { it.split("=") }
            ?.forEach {
                if (it.size != 2)
                    throw ParseQueryParamsException()
                if (it[0] in params)
                    throw ParseQueryParamsException()
                params[it[0]] = it[1]
            }
    }

    fun ensureAllUsed() {
        params.keys.forEach {
            if (it !in usedParams)
                throw ParseQueryParamsException()
        }
    }

    fun getString(name: String): String {
        val result = params[name] ?: throw ParseQueryParamsException()
        usedParams.add(name)
        return result
    }

    fun getBigDecimal(name: String): BigDecimal {
        val result = params[name]?.toBigDecimalOrNull() ?: throw ParseQueryParamsException()
        usedParams.add(name)
        return result
    }
}

val logger = Logger.getLogger("")

class Response(
    val code: Int,
    private val body: String = "",
    private val headers: Map<String, String> = emptyMap()) {

    fun write(http: HttpExchange) {
        http.sendResponseHeaders(code, body.toByteArray().size.toLong())
        headers.forEach { k, v ->  http.responseHeaders[k] = v }
        http.responseBody.write(body.toByteArray())
        http.responseBody.close()
    }
}

typealias HandlerImpl = (method: String, path: String, params: QueryParams) -> Response

fun handler(http: HttpExchange, handlerImpl: HandlerImpl) {
    val request = http.requestMethod + " " + http.requestURI.path +
        if (http.requestURI.query != null)
            "?${http.requestURI.query }"
        else
            ""
    try {

        logger.info(request)

        val params = QueryParams(http.requestURI.query)
        val response = handlerImpl(http.requestMethod, http.requestURI.path, params)

        response.write(http)

        if (response.code > 299) {
            logger.severe("$request : ${response.code}")
        } else {
            logger.info("$request : ${response.code}")
        }

    } catch (e: AccountDoesntExistException) {
        Response(404).write(http)
    } catch (e: ParseQueryParamsException) {
        Response(400).write(http)
    } catch (e: Exception) {
        Response(500).write(http)
    }
}

fun accountsHandler(method: String, suffix: String, params: QueryParams): Response {
    return when (method) {
        "POST" -> {
            if (suffix != "") {
                Response(405, headers = mapOf("Allow" to "GET"))
            } else {
                val amount = params.getBigDecimal("amount")
                params.ensureAllUsed()

                val acc = createAccount(amount)

                Response(201, acc)
            }
        }

        "GET" -> {
            if (suffix == "") {
                Response(405, headers = mapOf("Allow" to "POST"))
            } else {
                params.ensureAllUsed()

                val amount = getAccount(suffix)

                Response(200, "$amount")
            }
        }

        else -> Response(405, headers=mapOf("Allow" to "GET, POST"))
    }
}

fun transfersHandler(method: String, suffix: String, params: QueryParams): Response {
    return when (method) {
        "POST" -> {
            if (suffix != "") {
                Response(404)
            } else {
                val sender = params.getString("sender")
                val receiver = params.getString("receiver")
                val amount = params.getBigDecimal("amount")
                params.ensureAllUsed()

                transferMoney(sender, receiver, amount)

                Response(200)
            }
        }

        else -> Response(405, headers=mapOf("Allow" to "POST"))
    }
}

fun startServer() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    server.createContext("/v1/accounts/") {
        http -> handler(http) {
            method, path, params -> accountsHandler(method, path.substring("/v1/accounts/".length), params)
        }
    }
    server.createContext("/v1/transfers/") {
        http -> handler(http) {
            method, path, params -> transfersHandler(method, path.substring("/v1/transfers/".length), params)
        }
    }

    server.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    logger.info("Start server")
    server.start()
}
