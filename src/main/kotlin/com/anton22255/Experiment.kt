package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.blockchain.ChainType
import com.anton22255.db.DataBase
import com.anton22255.transport.Message
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.HashMap

class Experiment(val initData: InitData, val fixedThreadPoolContext : ExecutorCoroutineDispatcher) {

    val statisticResult = Statistic(initData.periodCount.toInt())
    val messageQueue = HashMap<Long, Queue<Message>>()
    val populationUtils = PopulationUtils(initData, statisticResult)

    fun startExperiment(): StatisticResult {
        var population = populationUtils.initPopulation()

//        print(population.size)
//        println()

        val timer = System.currentTimeMillis()

        (1..initData.periodCount).forEach { time ->
//            if (time.rem(10) == 0L) {
//                println("time $time")
//                println("time of processing ${System.currentTimeMillis() - timer} mls")
//            }
            val transactionMessages = populationUtils.generateTransactions(population, time)
            addMessages(time, transactionMessages)

            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), initData.period)
            populationUtils.createMinerBlocks(choseLuckyAgents, population, time)

            population.forEach {
                val messages = it.sendMessage()
                messages.forEach {
                    addMessage(it.expiredTime, it)
                }
                it.clearMessage()
            }

            processMessageCorutines(time, population)

            population = populationUtils.updatePopulation(population)


//            println("population numbers " + population.size)
        }
        val time = System.currentTimeMillis() - timer
        println("time of processing $time mls")
        return StatisticResult(initData, time, statisticResult.forkCounters.map { it.toLong() })
    }

    private fun addMessages(time: Long, messages: List<Message>) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).addAll(messages)
    }

    private fun addMessage(time: Long, message: Message) {
        messageQueue.getOrPut(time, { ConcurrentLinkedQueue() }).add(message)
    }

    private fun processMessageCorutines(
        time: Long,
        population: MutableList<Agent>
    ) {

        runBlocking<Unit>(fixedThreadPoolContext) {
            val jobs = messageQueue[time]?.map { message ->
                launch {
                    populationUtils.processMessage(population, message)
                }
            }
            jobs?.forEach { it.join() }
        }
    }
}

data class InitData(
    val transactionInOneRound: Int = 1000,
    val initN: Int = 1000,
    val channelMinCount: Int = 20,
    val maxHashAgentRate: Long = 10000L,
    val chainType: ChainType = ChainType.ANT,

    val diedAlpha: Double = 0.01,
    val liveAlpha: Double = 0.02,

    val sendTime: Int = 3,
    val sendBlockTime: Double = 3.0,

    val period: Int = 10,
    val periodCount: Long = 100
)