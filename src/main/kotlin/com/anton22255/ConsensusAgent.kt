package com.anton22255

import kotlin.math.roundToInt
import kotlin.random.Random

fun choseLuckyAgents(agentProbabilities: List<Double>, periodProbability: Double): ArrayList<Int> {
    val random = Random
    val agents = ArrayList<Int>()
    agentProbabilities.forEachIndexed { index, probability ->
        if (random.nextDouble(0.0, periodProbability) <= probability) {
            agents.add(index)
        }
    }
    return agents
}


fun choseLuckyAgents(agentHashRates: List<Int>, period: Int): ArrayList<Int> {
    val random = Random
    val agents = ArrayList<Int>()
    val sumHashRate = agentHashRates.sum()
    val until = 1.0.div(period)
    agentHashRates.map { rate ->
        rate.toDouble().div(sumHashRate).div(period)
    }
        .forEachIndexed { index, probability ->
            if (random.nextDouble(0.0, until).compareTo(probability) <=0 ) {
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
            if (index != indexOfChannel) {
                agent.addChanel(agents[indexOfChannel])
            }
        }
    }
}

fun removeChannels(agents: List<Agent>, diedAgents: List<Agent>): List<Agent> {
    agents.forEach { it.deleteChannels(diedAgents) }
    return agents
}











