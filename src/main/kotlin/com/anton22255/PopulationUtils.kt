package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.agent.HonestAgent
import com.anton22255.blockchain.createChain
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import kotlin.random.Random

class PopulationUtils(val initData: InitData) {

    fun createAgent(): HonestAgent {
        val hashRate = Random.nextLong(initData.maxHashAgentRate)
        return HonestAgent(hashRate, createChain(initData.chainType), initData)
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

        val burnAdditionalPopulation = addPopulation(initData.liveAlpha, populationSize) {
            createAgent()
        }

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


}
