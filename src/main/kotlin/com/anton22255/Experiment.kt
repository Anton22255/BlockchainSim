package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.blockchain.ChainType
import com.anton22255.graph.GraphVisualisation
import com.anton22255.graph.graphExplore
import com.anton22255.transport.Message
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.HashMap

class Experiment(val initData: InitData, val fixedThreadPoolContext: ExecutorCoroutineDispatcher) {

    val statisticResult = Statistic(initData.periodCount.toInt())
    val expendedStatistic: ExpendedStatistic = ExpendedStatistic()
    val messageQueue = HashMap<Long, Queue<Message>>()
    val populationUtils = PopulationUtils(initData, statisticResult)

    fun startExperiment(): StatisticResult {
        var population = populationUtils.initPopulation()

//        graphExplore(population)

        val timer = System.currentTimeMillis()

        (0 until initData.periodCount).forEach { time ->
            val transactionMessages = populationUtils.generateTransactions(population, time)
            addMessages(time, transactionMessages)


            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), initData.period)
            if (time.rem(10) == 0L) {
                print("_ ")

//                println( choseLuckyAgents.joinToString())
            }

//            println(messageQueue.values.map { it.size }.joinToString())

            populationUtils.createMinerBlocks(choseLuckyAgents, population, time)

            population.forEach {
                val messages = it.sendMessage()
                messages.forEach { message ->
                    message?.let {
                        addMessage(message.expiredTime, message)
                    }
                }
                it.clearMessage()
            }

//            processMessage(time, population)
            processMessageCorutines(time, population)

            statisticResult.setCommonNumber(populationUtils.compareChains(population))
            statisticResult.setForkNumber(populationUtils.forkChains(population))

            if (initData.needExpendedStatistic) {
                expendedStatistic.resultMatrix
                    .add(populationUtils.headStatistics(population.map { it.blockChain.getMainVersion() }))
            }

            population = populationUtils.updatePopulation(population)
        }
        val time = System.currentTimeMillis() - timer
        println("time of processing $time mls")

        if (initData.needExpendedStatistic) {
            writeStatistics(initData, expendedStatistic)
        }

        println("time : $time; size :  ${population.size} ")
        return StatisticResult(
            initData, time, statisticResult.forkNewCounters.map { it.toLong() },
            statisticResult.tailCounters
        )
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

//    suspend fun processMessage(
//        time: Long,
//        population: MutableList<Agent>
//    ) {
//
//        messageQueue[time]?.map { message ->
//            populationUtils.processMessage(population, message)
//        }
//
//    }
}

data class InitData(
    val transactionInOneRound: Int = 1000,
    val initN: Int = 1000,
    val channelMinCount: Int = 20,
    val maxHashAgentRate: Long = 10000L,
    val minHashAgentRate: Long = 100L,

    val chainType: ChainType = ChainType.ANT,

    val diedAlpha: Double = 0.01,
    val liveAlpha: Double = 0.02,

    val sendTime: Int = 4,
    val sendBlockTime: Double = 3.0,

    val period: Int = 10,
    val periodCount: Long = 100,

    val needExpendedStatistic: Boolean = false

) {
    fun toString1(): String {
        return "$transactionInOneRound,$initN,$channelMinCount,$maxHashAgentRate,$minHashAgentRate,$chainType,=$diedAlpha,=$liveAlpha,=$sendTime,=$sendBlockTime,=$period,=$periodCount,=$needExpendedStatistic"
    }
}