import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.random.nextInt
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.concurrent.thread

import accounts.getAccount
import accounts.createAccount
import accounts.transferMoney


fun runInParallel(f: () -> Unit) {
    val jobs = List(10) {
        thread {
            repeat (1_000_000 ) {
                f()
            }
        }
    }

    jobs.forEach { it.join() }
}

class Accounts {
    var total = BigDecimal(0)
    var accounts = mutableListOf<String>()
    private val accountsLock = ReentrantReadWriteLock()

    fun add(acc: String) {
        accountsLock.write {
            accounts.add(acc)
            total += BigDecimal(1)
        }
    }

    fun getRandom(): Pair<String, String>? {
        accountsLock.read {
            if (accounts.size != 0) {
                val sender = accounts[Random.nextInt(0..accounts.size - 1)]
                val receiver = accounts[Random.nextInt(0..accounts.size - 1)]
                return Pair(sender, receiver)
            }
            return null
        }
    }
}

class TestsAccounts {
    @Test
    fun stressTestTransfersOnly() {
        val accounts = List(1000) {
            createAccount(BigDecimal(1))
        }

        runInParallel {
            val sender = accounts[Random.nextInt(0..accounts.size - 1)]
            val receiver = accounts[Random.nextInt(0..accounts.size - 1)]
            transferMoney(sender, receiver, BigDecimal(1))
        }

        val total = accounts.map { getAccount(it) }.fold(BigDecimal.ZERO, {x, y -> x + y})
        Assertions.assertEquals(BigDecimal(1000), total)
    }

    @Test
    fun stressTestCreatesAndTransfers() {
        var accounts = Accounts()
        runInParallel {
            val op = Random.nextInt(1..1000)
            when (op) {
                in 1..1 -> {
                    accounts.add(createAccount(BigDecimal(1)))
                }
                else -> {
                    val accs = accounts.getRandom()
                    if (accs != null) {
                        val (sender, receiver) = accs
                        transferMoney(sender, receiver, BigDecimal(1))
                    }
                }
            }
        }

        val total = accounts.accounts.map { getAccount(it) }.fold(BigDecimal.ZERO, {x, y -> x + y})
        Assertions.assertEquals(accounts.total, total)
    }
}
