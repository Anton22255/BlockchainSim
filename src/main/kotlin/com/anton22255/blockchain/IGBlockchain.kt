package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Statistic
import java.util.*
import kotlin.math.min

class IGBlockchain : Chain {
    override lateinit var statistic: Statistic

    var mainBlocks: MutableList<Block> = arrayListOf(createGenesisBlock())

    override fun getLastBlock(): Block = mainBlocks.last()

    override fun addBlock(block: Block): ChainAnswer {
        return if (block.depth <= getLastBlock().depth) {
            ChainAnswer.DECLINE
        } else {
            if (block.prevHash == getLastBlock().calculateHash()) {
                mainBlocks.add(block)
                ChainAnswer.ACCEPT.apply {
                    data = block
                }
            } else {
                ChainAnswer.REQUEST.apply {
                    data = mainBlocks.map { it.calculateHash() }.toList()
                }
            }
        }
    }

    override fun requestData(request: Any): ChainAnswer {
        val firstDif = (request as? List<String>)?.let { hashes ->
            findFirstDifIndex(hashes, getMainVersion())
        } ?: 1
        return ChainAnswer.ANSWER.apply {
            data = if(mainBlocks.size > firstDif) {
                mainBlocks.subList(firstDif, mainBlocks.size).map { it.copy() }
            }else{
                emptyList()
            }
        }
    }

    fun findFirstDifIndex(hashes: List<String>, hashes2: List<String>) =
        (0 until min(hashes.size, hashes2.size))
            .firstOrNull { hashes[it] != hashes2[it] } ?: hashes.lastIndex

    override fun answerData(answer: Any): ChainAnswer {

            (answer as? List<Block>)?.let {
                if(it.isEmpty()){
                    return ChainAnswer.DECLINE
                }
                if (it.lastOrNull()?.depth?:0 >= mainBlocks.last().depth) {

                    mainBlocks.removeAll(mainBlocks.takeLast(mainBlocks.last().depth - it.first().depth + 1))
//                mainBlocks = mainBlocks.dropLast(mainBlocks.last().depth - it.first().depth + 1).toMutableList()
//                mainBlocks.addAll(it)
                    mainBlocks.addAll(it)
                    return ChainAnswer.ACCEPT.apply {
                        data = mainBlocks.last()
                    }

                } else {
                    return ChainAnswer.DECLINE
                }
            }
            return ChainAnswer.DECLINE
    }

    override fun getMainVersion(): List<String> = mainBlocks.map { it.calculateHash() }

    override fun copy(): Chain =
        IGBlockchain().apply {
            mainBlocks.clear()
            mainBlocks.addAll(mainBlocks.toMutableList())
        }
}