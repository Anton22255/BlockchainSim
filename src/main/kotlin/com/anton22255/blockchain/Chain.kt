package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Transaction

interface Chain {

    fun getLastBlock(): Block

    fun addBlock(block: Block): ChainAnswer

    fun requestData(request: Any): ChainAnswer

    fun answerData(answer: Any): ChainAnswer

    fun copy(): Chain
}

fun createGenesisBlock() = Block(listOf(Transaction()))

enum class ChainAnswer {
    ACCEPT,
    DECLINE,
    ANSWER,
    REQUEST;

    lateinit var data: Any
}