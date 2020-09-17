package com.anton22255

import com.anton22255.blockchain.ChainType
import com.anton22255.db.DataBase
import com.anton22255.graph.GraphVisualisation
import com.anton22255.transport.Message
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext
import java.util.*
import kotlin.collections.HashMap

object Main {

    private const val period: Int = 10
    private const val periodCount: Long = 101

    const val transactionInOneRound: Int = 100
    const val initN = 103
    const val channelMinCount = 20
    const val maxHashAgentRate = 10000L
    val chainType = ChainType.ANT

    const val diedAlpha = 0.01
    const val liveAlpha = 0.02

    const val sendTime = 3
    const val sendBlockTime = 3

    val messageQueue = HashMap<Long, Queue<Message>>()
    val dataBase = DataBase()

    @JvmStatic
    fun main(args: Array<String>) {

        val newFixedThreadPoolContext = newFixedThreadPoolContext(10, "background")
        val alphaVariants
                = arrayOf(0.000)
//                = (1..5).map { it / 100.0 }
        val liveAlphaVariants
                = arrayOf(0.00)
//                = (1..5).map { it / 100.0 }
        val sendTimeRange
//                = arrayOf(1)
                = arrayOf(1, 2, 4, 8, 16)
//                = 1..5
        val sendBlockTimeRange
                = arrayOf(0.1)
//                = arrayOf(0.1, 0.5, 1.0, 3.0)
//                = arrayOf(1.0, 1.5, 2.0, 2.5)
        val channelsRange
//                = arrayOf(9)
                = arrayOf(initN.times(0.5).toInt(), initN - 2, initN - 3, initN-1)
//                = arrayOf(initN - 1)
//            , initN.times(0.5).toInt(), initN.times(0.8).toInt())
//                    = arrayOf(100, 1000, 2000, 5000, 8000, 10000)
        val arrayOfChainTypes
//                = arrayListOf(ChainType.IG)
                = arrayListOf(ChainType.ANT)
//                = ChainType.values()
        val periodRange
                = arrayOf(2, 4, 8, 16)
//                = arrayOf(2)

        var counter = 1;
        val countAllVariants =
//            arrayOfChainTypes.size*
            periodRange.size * alphaVariants.size * liveAlphaVariants.size * sendTimeRange.count() * channelsRange.count() * (
                    1 + sendBlockTimeRange.size)


        for (periodInd in periodRange) {
            for (chainType in arrayOfChainTypes) {

                for (diedAlpha in alphaVariants) {
                    for (liveAlpha in liveAlphaVariants) {
                        for (sendTime in sendTimeRange) {
                            for (sendBlockTime in
                            if (chainType == ChainType.IG) sendBlockTimeRange else arrayOf(1.0)) {

                                for (channelsCount in channelsRange) {
                                    println(
                                        "$counter/$countAllVariants  (${
                                        "%.3f".format(counter.toDouble().div(countAllVariants).times(100))})"
                                    )

                                    counter++
                                    val initData = InitData(
                                        period = periodInd,
                                        periodCount = periodCount,
                                        transactionInOneRound = transactionInOneRound,
                                        initN = initN,
                                        channelMinCount = channelsCount,
                                        maxHashAgentRate = 100L,
                                        minHashAgentRate = 10L - 1,
                                        chainType = chainType,
                                        diedAlpha = diedAlpha,
                                        liveAlpha = liveAlpha,
                                        sendTime = sendTime,
                                        sendBlockTime = sendBlockTime,
                                        needExpendedStatistic = false
                                    )

                                    runExperiment(initData, newFixedThreadPoolContext)
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    private fun runExperiment(
        initData: InitData,
        newFixedThreadPoolContext: ExecutorCoroutineDispatcher
    ) {
//        if (!dataBase.experimentExist(initData)) {


//        (1..5).forEach {
            val experiment = Experiment(
                initData = initData,
                fixedThreadPoolContext = newFixedThreadPoolContext
            )
            val startExperiment = experiment.startExperiment()
            dataBase.writeExperiment(startExperiment)
//        }
    }
//    }
}