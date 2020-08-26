package com.anton22255.blockchain

import com.anton22255.InitData
import com.anton22255.PopulationUtils
import com.anton22255.Statistic
import com.anton22255.choseLuckyAgents
import org.junit.Test
import kotlin.test.assertEquals

internal class RoundTest {

    private val periodCount: Long = 100
    private val period: Int = 10

    @Test
    fun chooseTest() {

        val population = PopulationUtils(InitData(), Statistic(10)).initPopulation()

        (1..periodCount).forEach { _ ->
            val choseLuckyAgents = choseLuckyAgents(population.map { it.hashRate }.toList(), period)
            print("lucky agents " + choseLuckyAgents.joinToString(separator = " "))
            println()
        }
    }

    @Test
    fun countForkTest() {

        val chain1 = listOf("a", "b", "c")
        val chain2 = listOf("a", "b", "c")
        val chain3 = listOf("a", "b", "c", "d")

        val chains = HashSet<List<String>>()

        chains.add(chain1)
        chains.add(chain2)
        chains.add(chain3)

        val chains2 = hashSetOf(chain1, chain3)

        assertEquals(2, chains.size)
        assertEquals(chains2, chains)

    }
}