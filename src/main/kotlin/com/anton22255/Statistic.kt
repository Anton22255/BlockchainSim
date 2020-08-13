package com.anton22255

import java.util.concurrent.atomic.AtomicInteger

class Statistic(periodCount: Int) {

    val forkCounters = MutableList(periodCount) { AtomicInteger(0) }
    val tailCounters = arrayListOf<Long>()

    fun incrementForkCount(period: Int) {
        forkCounters?.getOrNull(period)?.getAndUpdate { it.inc() }
//            ?: forkCounters.add(period, AtomicInteger(1))
    }

    fun setCommonNumber(value: Int) = tailCounters.add(value.toLong())
}

data class StatisticResult(
    val initData: InitData,
    val time: Long,
    val forkCounters: List<Long>,
    val tailCounters: List<Long>
)

data class ExpendedStatistic(
    var resultMatrix: MutableList<Array<Int>> = arrayListOf()
)