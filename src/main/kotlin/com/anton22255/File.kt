package com.anton22255

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun writeStatistics(initData: InitData, expendedStatistic: ExpendedStatistic) {

    val fileName = "./logs/logs_${initData.toString1()}.csv"
    val printWriter = File(fileName)
        .printWriter()

//    var file = File(fileName)
    //     create a new file
//    val isNewFileCreated: Boolean = file.createNewFile()

    for (line in expendedStatistic.resultMatrix) {
        printWriter.println(
            line.joinToString(separator = ";")
        )
    }
    printWriter.close()
}

