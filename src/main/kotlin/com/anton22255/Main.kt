package com.anton22255

import com.anton22255.blockchain.ChainType
import com.anton22255.db.DataBase
import com.anton22255.transport.Message
import kotlinx.coroutines.newFixedThreadPoolContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.sign

object Main {

    private const val period: Int = 10
    private const val periodCount: Long = 100

    const val transactionInOneRound: Int = 1000
    const val initN = 1000
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
//                = arrayOf(0.02)
                = (1..5).map { it / 100.0 }
        val liveAlphaVariants
//                = arrayOf(0.05)
                = (1..5).map { it / 100.0 }
        val sendTimeRange
//                = arrayOf(2)
                = 1..5
        val sendBlockTimeRange
//                = arrayOf(0.1)
                = arrayOf(1.0, 1.5, 2.0, 2.5)
        val channelsRange = arrayOf(200, 500, 900)
//                    = arrayOf(100, 1000, 2000, 5000, 8000, 10000)
        val arrayOfChainTypes
//                = arrayListOf(ChainType.IG)
                = ChainType.values()

        var counter = 1;
        val countAllVariants =
            arrayOfChainTypes.size * alphaVariants.size * liveAlphaVariants.size * sendBlockTimeRange.size * sendTimeRange.count() * channelsRange.count()

        for (chainType in arrayOfChainTypes) {

            for (diedAlpha in alphaVariants) {
                for (liveAlpha in liveAlphaVariants) {
                    for (sendTime in sendTimeRange) {
                        for (sendBlockTime in sendBlockTimeRange) {

                            for (channelsCount in channelsRange) {
                                println(
                                    "$counter/$countAllVariants  (${
                                    "%.3f".format(counter.toDouble().div(countAllVariants).times(100))})"
                                )

                                counter++
                                val initData = InitData(
                                    period = 10,
                                    periodCount = 31,
                                    transactionInOneRound = 1000,
                                    initN = 1000,
                                    channelMinCount = channelsCount,
                                    maxHashAgentRate = 10000L,
                                    chainType = chainType,
                                    diedAlpha = diedAlpha,
                                    liveAlpha = liveAlpha,
                                    sendTime = sendTime,
                                    sendBlockTime = sendBlockTime
                                )

                                if (!dataBase.experimentExist(initData)) {

                                    val experiment = Experiment(
                                        initData = initData,
                                        fixedThreadPoolContext = newFixedThreadPoolContext
                                    )
                                    val startExperiment = experiment.startExperiment()
                                    dataBase.writeExperiment(startExperiment)
                                }
                            }

                        }
                    }
                }
            }
        }

    }
}