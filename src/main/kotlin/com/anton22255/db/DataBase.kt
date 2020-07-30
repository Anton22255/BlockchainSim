package com.anton22255.db


import com.anton22255.InitData
import com.anton22255.StatisticResult
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DataBase {
    val database = Database.connect(
        "jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver",
        user = "postgres"
    )

    fun writeExperiment(statisticResult: StatisticResult) {

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create<Table>(Experiments)

            val id = Experiments.insert {
                it[transactionInOneRound] = statisticResult.initData.transactionInOneRound
                it[initN] = statisticResult.initData.initN
                it[channelMinCount] = statisticResult.initData.channelMinCount
                it[maxHashAgentRate] = statisticResult.initData.maxHashAgentRate

                it[chainType] = statisticResult.initData.chainType.name
                it[diedAlpha] = statisticResult.initData.diedAlpha
                it[liveAlpha] = statisticResult.initData.liveAlpha
                it[sendTime] = statisticResult.initData.sendTime
                it[sendBlockTime] = statisticResult.initData.sendBlockTime
                it[period] = statisticResult.initData.period
                it[periodCount] = statisticResult.initData.periodCount

                it[time] = statisticResult.time
                it[status] = "OK"
                it[result_fork] = statisticResult.forkCounters
            } get Experiments.id

        }
    }

    fun experimentExist(initData: InitData): Boolean {
        var exist = false
        transaction {
//            addLogger(StdOutSqlLogger)
            exist = (Experiments.exists() &&
                    Experiments.select {
                        (Experiments.diedAlpha eq initData.diedAlpha)
                            .and(Experiments.liveAlpha eq initData.liveAlpha)
                            .and(Experiments.initN eq initData.initN)
                            .and(Experiments.sendBlockTime eq initData.sendBlockTime)
                            .and(Experiments.sendTime eq initData.sendTime)
                            .and(Experiments.period eq initData.period)
                            .and(Experiments.periodCount eq initData.periodCount)
                            .and(Experiments.chainType eq initData.chainType.name)
                            .and(Experiments.channelMinCount eq initData.channelMinCount)
                            .and(Experiments.initN eq initData.initN)

                    }.toList().isNotEmpty())
        }
        return exist
    }

}

object Experiments : Table() {

    val id = integer("id").autoIncrement() // Column<Int>

    val transactionInOneRound = integer("transactionInOneRound")
    val initN = integer("initN")
    val channelMinCount = integer("channelMinCount")
    val maxHashAgentRate = long("maxHashAgentRate")
    val chainType = varchar("chainType", 50)

    val diedAlpha = double("diedAlpha")
    val liveAlpha = double("liveAlpha")

    val sendTime = integer("sendTime")
    val sendBlockTime = double("sendBlockTime")

    val period = integer("period")
    val periodCount = long("periodCount")

    val status = varchar("status", 50)

    val time = long("time")

    val result_fork = arrayOfLong("result_fork")

}
