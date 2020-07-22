package com.anton22255

import com.anton22255.blockchain.AntBlockChain
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import javafx.application.Application.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte0.runnable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

object Main {

    private const val initN = 100
    private const val channelMinCount = 5
    private const val maxHashAgentRate = 1000
    private const val period: Int = 200
    private const val transactionInOneRound: Int = 100
    private const val periodCount: Long = 500

    const val diedAlpha = 0.01
    const val liveAlpha = 0.02

    const val sendTime = 3

    var systemHashRate: Long = 0

    val messageQueue = HashMap<Long, ConcurrentLinkedQueue<Message>>()

    @JvmStatic
    fun main(args: Array<String>) {

        var population = initPopulation()
        print(population.size)
        println()

        val timer = System.currentTimeMillis()
        (1..periodCount).forEach { time ->

            if (time.rem(100) == 0L) {
                println("time $time")
                println("time of processing ${System.currentTimeMillis() - timer} mls")
            }
            generateTransactions(population, time)

            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), period)
            createMinerBlocks(choseLuckyAgents, population, time)

            population.forEach {
                val messages = it.sendMessage()
                messages.forEach {
                    addMessage(it.expiredTime, it)
                }
                it.clearMessage()
            }

            runBlocking<Unit> {
                val jobs = messageQueue[time]?.map { message ->
                    launch {
                        processMessage(population, message)
                    }
                }
                jobs?.forEach { it.join() }
            }
//            messageQueue[time]
//                ?.forEach { message -> processMessage(population, message) }

            population = updatePopulation(population)
//            println("population numbers " + population.size)
        }

        println("time of processing ${System.currentTimeMillis() - timer} mls")
    }

    private fun processMessage(
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

    private fun generateTransactions(population: List<Agent>, time: Long) {

        (1..transactionInOneRound)
            .map {
                val sender = population[Random.nextInt(population.size)]
                val recipient = population[Random.nextInt(population.size)]

                val transaction = Transaction(
                    originator = sender.id,
                    content = "From ${sender.id} to ${recipient.id} 100",
                    signature = sender.id.toByteArray()
                )

                Message(
                    type = Type.TRANSACTION,
                    data = transaction,
                    senderId = sender.id,
                    recipientId = sender.id,
                    initTime = time,
                    expiredTime = 0
                )
            }
            .toList()
            .let {
                addMessages(time, it)
            }
    }

    private fun addMessages(time: Long, messages: List<Message>) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).addAll(messages)
    }

    private fun addMessage(time: Long, message: Message) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).add(message)
    }

    private fun updatePopulation(population: MutableList<Agent>): MutableList<Agent> {
        val populationSize = population.size
        val selectPopulation = selectPopulation(diedAlpha, population)
        population.removeAll(selectPopulation)
        removeChannels(population, selectPopulation)

        val burnAdditionalPopulation = addPopulation(liveAlpha, populationSize) {
            createAgent()
        }

        population.addAll(burnAdditionalPopulation)
        initChannels(population, channelMinCount)
        return population
    }

    fun initPopulation(): MutableList<Agent> {

        val population = addPopulation(1.0, initN) {
            createAgent()
        }

        initChannels(population, channelMinCount)
        return population as MutableList<Agent>
    }

    private fun createAgent(): HonestAgent {
        val hashRate = Random.nextInt(maxHashAgentRate)
        systemHashRate.plus(hashRate)

        return HonestAgent(hashRate, AntBlockChain())
    }
}