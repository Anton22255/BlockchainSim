package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.agent.HonestAgent
import com.anton22255.blockchain.createChain
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.min
import kotlin.random.Random

class PopulationUtils(val initData: InitData, val statistic: Statistic) {

    fun createAgent(): HonestAgent {
        val hashRate = Random.nextLong(initData.maxHashAgentRate)
        val blockChain = createChain(initData.chainType)
        return HonestAgent(hashRate, blockChain, initData, statistic)
    }

    fun initPopulation(): MutableList<Agent> {
        val population = addPopulation(1.0, initData.initN) { createAgent() }
        initChannels(population, initData.channelMinCount)
        return population as MutableList<Agent>
    }

    fun updatePopulation(population: MutableList<Agent>): MutableList<Agent> {
        val populationSize = population.size
        val selectPopulation = selectPopulation(initData.diedAlpha, population)
        population.removeAll(selectPopulation)
        removeChannels(population, selectPopulation)

        val burnAdditionalPopulation = addPopulation(initData.liveAlpha, populationSize) { createAgent() }

        population.addAll(burnAdditionalPopulation)
        initChannels(population, initData.channelMinCount)

        return population
    }

    fun generateTransactions(population: List<Agent>, time: Long) =

        (1..initData.transactionInOneRound)
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

    fun createMinerBlocks(
        choseLuckyAgents: ArrayList<Int>,
        population: MutableList<Agent>,
        time: Long
    ) {
        choseLuckyAgents.forEach { index -> population[index].createBlock(time) }
    }

    fun processMessage(
        population: MutableList<Agent>,
        message: Message
    ) {
        population.find {
            message.recipientId == it.id
        }?.receiveMessage(message)
    }

    fun compareChains(population: MutableList<Agent>): Int {

        val mainVersion = population.first().blockChain.getMainVersion()
        val list = population.fold(mainVersion) { acc, item -> commonPart(acc, item.blockChain.getMainVersion()) }

        return list.size
    }


    fun commonPart(partA: List<String>, partB: List<String>): List<String> {
        val result = ArrayList<String>()
        for (i in 0 until (min(partA.size, partB.size))) {
            if (partA[i] == partB[i]) {
                result.add(partA[i])
            } else {
                return result
            }
        }
        return result
    }
}
