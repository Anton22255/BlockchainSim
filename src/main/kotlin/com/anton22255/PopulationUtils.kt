package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.agent.HonestAgent
import com.anton22255.blockchain.createChain
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import kotlin.math.min
import kotlin.random.Random

class PopulationUtils(val initData: InitData, val statistic: Statistic) {

    fun createAgent(): HonestAgent {
        val hashRate = Random.nextLong(initData.minHashAgentRate, initData.maxHashAgentRate)
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

        return compareChains1(population.map { it.blockChain.getMainVersion() })
    }

    fun compareChains1(chains: List<List<String>>): Int {

//        println(" ============  ")
//        println(chains.first())
        val list = chains.fold(chains.first()) { acc, item -> commonPart(acc, item) }
//        println(list.joinToString())
        return list.size
    }

    fun headStatistics(chains: List<List<String>>): Array<Int> {

        val result: Array<Int> = Array(chains.size * (chains.size - 1) / 2) { 0 }

        var index = 0
        for (i in 0 until (chains.size - 1)) {
            for (j in i + 1 until chains.size) {
                val part = commonPart(chains[i], chains[j])
                result[index] = part.size
                index++
            }
        }
        return result
    }

    fun commonPart(partA: List<String>, partB: List<String>): List<String> {
        val result = ArrayList<String>()
//        println(partB)
        for (i in 0 until (min(partA.size, partB.size))) {
            if (partA[i].contentEquals(partB[i])) {
                result.add(partA[i])
            } else {

                return result
            }
        }
//        print( " $result")
        return result
    }
}
