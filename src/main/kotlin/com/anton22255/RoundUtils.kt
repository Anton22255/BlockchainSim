package com.anton22255

import com.anton22255.agent.Agent
import kotlin.math.roundToInt
import kotlin.random.Random

fun choseLuckyAgents(agentHashRates: List<Long>, period: Int): ArrayList<Int> {
    val random = Random
    val agents = ArrayList<Int>()
    val sumHashRate = agentHashRates.sum()
    val until = 1.0.div(period)
    agentHashRates.map { rate ->
        rate.toDouble().div(sumHashRate).div(period)
    }
        .forEachIndexed { index, probability ->
            if (random.nextDouble(0.0, until).compareTo(probability) <= 0) {
                agents.add(index)
            }
        }
    return agents
}

fun selectPopulation(diedAlpha: Double, agents: List<Agent>): List<Agent> {
    val size = agents.size
    return agents.shuffled().dropLast((size * (1 - diedAlpha)).toInt())
}

fun addPopulation(burnAlpha: Double, size: Int, burnFunction: () -> Agent): List<Agent> {
    return (1..((size * burnAlpha).roundToInt()))
        .map {
            burnFunction()
        }
}

fun initChannels(agents: List<Agent>, linkCount: Int) {
    agents.forEachIndexed { index, agent ->
        while (agent.channels.size < linkCount) {
            val indexOfChannel = Random.nextInt(agents.size)
            val notContain = agent.channels.filter { it.id.contentEquals(agents[indexOfChannel].id) }.isEmpty()
            if (index != indexOfChannel && notContain) {
                agent.addChanel(agents[indexOfChannel])

                if(!agents[indexOfChannel].channels.contains(agent)) {
                    agents[indexOfChannel].addChanel(agent)
                }
            }
        }
    }
}

fun removeChannels(agents: List<Agent>, diedAgents: List<Agent>): List<Agent> {
    agents.forEach { it.deleteChannels(diedAgents) }
    return agents
}











