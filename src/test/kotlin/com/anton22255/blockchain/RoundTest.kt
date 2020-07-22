package com.anton22255.blockchain

import com.anton22255.Main
import com.anton22255.choseLuckyAgents
import org.junit.Test

internal class RoundTest {

    private val periodCount: Long = 100
    private val period: Int = 10

    @Test
    fun chooseTest() {

        val population = Main.initPopulation()

        (1..periodCount).forEach { time ->
            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), period)
            print("lucky agents " + choseLuckyAgents.joinToString(separator = " "))
            println()
        }
    }
}