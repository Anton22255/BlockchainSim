package com.anton22255

import com.anton22255.PopulationUtils.createAgent
import com.anton22255.PopulationUtils.generateTransactions
import com.anton22255.PopulationUtils.initPopulation
import com.anton22255.PopulationUtils.updatePopulation
import com.anton22255.agent.Agent
import com.anton22255.agent.HonestAgent
import com.anton22255.blockchain.AntBlockChain
import com.anton22255.blockchain.ChainType
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.random.Random

object Main {

    const val initN = 1000
    const val channelMinCount = 5
    const val maxHashAgentRate = 1000L
    private const val period: Int = 20
    private const val periodCount: Long = 50
    const val transactionInOneRound: Int = 100
    val chainType = ChainType.ANT

    const val diedAlpha = 0.01
    const val liveAlpha = 0.02

    const val sendTime = 3
    const val sendBlockTime = 3

    var systemHashRate: Long = 0

    val messageQueue = HashMap<Long, ConcurrentLinkedQueue<Message>>()

    @JvmStatic
    fun main(args: Array<String>) {

        var population = initPopulation()
        systemHashRate = population.map { it.hashRate }.sum()

        print(population.size)
        println()

        val timer = System.currentTimeMillis()
        (1..periodCount).forEach { time ->

            if (time.rem(10) == 0L) {
                println("time $time")
                println("time of processing ${System.currentTimeMillis() - timer} mls")
            }
            val transactionMessages = generateTransactions(population, time)
            addMessages(time, transactionMessages)

            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), period)
            createMinerBlocks(choseLuckyAgents, population, time)

            population.forEach {
                val messages = it.sendMessage()
                messages.forEach {
                    addMessage(it.expiredTime, it)
                }
                it.clearMessage()
            }

            processMessageCorutines(time, population)

//            processMessage(time, population)

//            simpleMessageProcess(time, population)

            population = updatePopulation(population)
//            println("population numbers " + population.size)
        }

        println("time of processing ${System.currentTimeMillis() - timer} mls")
    }

    private fun simpleMessageProcess(
        time: Long,
        population: MutableList<Agent>
    ) {
        messageQueue[time]
            ?.forEach { message -> processMessage(population, message) }
    }

    private fun processMessageCorutines(
        time: Long,
        population: MutableList<Agent>
    ) {

        val fixedThreadPoolContext = newFixedThreadPoolContext(10, "background")

        runBlocking<Unit>(fixedThreadPoolContext) {
            val jobs = messageQueue[time]?.map { message ->
                launch {
                    processMessage(population, message)
                }
            }
            jobs?.forEach { it.join() }
        }
    }

    private fun processMessage(time: Long, population: MutableList<Agent>) {
        val executor = Executors.newFixedThreadPool(16)
        messageQueue[time]?.map { message ->
            val worker = Runnable { processMessage(population, message) }
            executor.execute(worker)
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
//        println("Finished all threads")
    }

    fun processMessage(
        population: MutableList<Agent>,
        message: Message
    ) {
        population.find {
            message.recipientId == it.id
        }?.receiveMessage(message)
    }

    private fun createMinerBlocks(
        choseLuckyAgents: ArrayList<Int>,
        population: MutableList<Agent>,
        time: Long
    ) {
        choseLuckyAgents.forEach { index -> population[index].createBlock(time) }
    }


    private fun addMessages(time: Long, messages: List<Message>) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).addAll(messages)
    }

    private fun addMessage(time: Long, message: Message) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).add(message)
    }
}