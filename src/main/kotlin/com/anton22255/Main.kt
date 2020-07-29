package com.anton22255

import com.anton22255.blockchain.ChainType
import com.anton22255.db.DataBase
import com.anton22255.transport.Message
import java.util.*
import kotlin.collections.HashMap

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

    @JvmStatic
    fun main(args: Array<String>) {

//        DataBase().init()

        val experiment = Experiment(
            initData = InitData(
                period = 10,
                periodCount = 100,
                transactionInOneRound = 1000,
                initN = 1000,
                channelMinCount = 20,
                maxHashAgentRate = 10000L,
                chainType = ChainType.ANT,

                diedAlpha = 0.01,
                liveAlpha = 0.02,

                sendTime = 3,
                sendBlockTime = 3
            )
        )
        experiment.startExperiment()

    }
}