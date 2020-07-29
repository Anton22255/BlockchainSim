package com.anton22255

import java.util.concurrent.atomic.AtomicInteger

class Statistic(periodCount: Int) {

    val forkCounters = MutableList(periodCount) { AtomicInteger(0) }

    fun incrementForkCount(period: Int) {
        forkCounters?.getOrNull(period)?.getAndUpdate { it.inc() }
            ?: forkCounters.add(period, AtomicInteger(1))
    }
}

data class StatisticResult(
    val initData: InitData,
    val time: Long,
    val forkCounters: List<Long>
)