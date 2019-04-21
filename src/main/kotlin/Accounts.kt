package accounts

import java.math.BigDecimal
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.UUID.randomUUID
import kotlin.collections.HashMap
import kotlin.concurrent.*


class AccountDoesntExistException(acc: String) : Exception(acc)

class AccountAlreadyExistsException(acc: String) : Exception(acc)


class Account(var amount : BigDecimal, val lock: ReentrantLock = ReentrantLock())

val accounts = HashMap<String, Account>()

val accountsLock = ReentrantReadWriteLock()


fun getAccountImpl(acc: String): Account {
    return accounts[acc] ?: throw AccountDoesntExistException(acc)
}

fun createAccount(amount : BigDecimal): String {
    val acc = randomUUID().toString()
    accountsLock.write {
        if (acc in accounts)
            throw AccountAlreadyExistsException(acc)

        accounts[acc] = Account(amount)
    }
    return acc
}

fun getAccount(acc: String): BigDecimal {
    accountsLock.read {
        val account = getAccountImpl(acc)
        account.lock.withLock {
            return account.amount
        }
    }
}

fun transferMoney(src: String, dst: String, amount: BigDecimal) {
    accountsLock.read {
        val srcAccount = getAccountImpl(src)
        val dstAccount = getAccountImpl(dst)

        val l1 = if (src < dst) srcAccount.lock else dstAccount.lock
        val l2 = if (src < dst) dstAccount.lock else srcAccount.lock

        l1.withLock {
            l2.withLock {
                srcAccount.amount -= amount
                dstAccount.amount += amount
            }
        }
    }
}


