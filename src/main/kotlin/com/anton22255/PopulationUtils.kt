package com.anton22255

import com.anton22255.agent.Agent
import com.anton22255.agent.HonestAgent
import com.anton22255.blockchain.AntBlockChain
import com.anton22255.blockchain.createChain
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import kotlin.random.Random

object PopulationUtils {

    fun createAgent(): HonestAgent {
        val hashRate = Random.nextLong(Main.maxHashAgentRate)
        return HonestAgent(hashRate, createChain(Main.chainType))
    }

    fun initPopulation(): MutableList<Agent> {
        val population = addPopulation(1.0, Main.initN) { createAgent() }
        initChannels(population, Main.channelMinCount)
        return population as MutableList<Agent>
    }

    fun updatePopulation(population: MutableList<Agent>): MutableList<Agent> {
        val populationSize = population.size
        val selectPopulation = selectPopulation(Main.diedAlpha, population)
        population.removeAll(selectPopulation)
        removeChannels(population, selectPopulation)

        val burnAdditionalPopulation = addPopulation(Main.liveAlpha, populationSize) {
            createAgent()
        }

        population.addAll(burnAdditionalPopulation)
        initChannels(population, Main.channelMinCount)
        return population
    }

    fun generateTransactions(population: List<Agent>, time: Long) =

        (1..Main.transactionInOneRound)
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

}
