package main

import server.*


fun main(args: Array<String>) {
    val port = if (args.size < 1) 3001 else args[0].toInt()
    startServer(port)
}