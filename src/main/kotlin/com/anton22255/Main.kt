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
        for (chainType in ChainType.values()) {

            val alphaVariants = (1..50).map { it / 100.0 }
            val liveAlphaVariants = (1..50).map { it / 100.0 }
            val sendTimeRange = 1..5
            val sendBlockTimeRange = arrayOf(1.0, 1.5, 2.0, 2.5)
            val channelsRange = arrayOf(100, 1000, 2000, 5000, 8000, 10000)

            var counter = 1;
            val countAllVariants =
                alphaVariants.size * liveAlphaVariants.size * sendBlockTimeRange.size * sendTimeRange.count()*channelsRange.count()

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
                                    periodCount = 100,
                                    transactionInOneRound = 1000,
                                    initN = 10000,
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