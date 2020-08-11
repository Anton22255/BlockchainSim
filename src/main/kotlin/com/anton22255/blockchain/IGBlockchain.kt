package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Statistic
import kotlin.math.min

class IGBlockchain : Chain {
    override lateinit var statistic: Statistic

    val mainBlocks: MutableList<Block> = arrayListOf(createGenesisBlock())

    override fun getLastBlock(): Block = mainBlocks.last()

    override fun addBlock(block: Block): ChainAnswer {
        synchronized(this) {

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
    }

    override fun requestData(request: Any): ChainAnswer {
        val firstDif = (request as? List<String>)?.let { hashes ->
            findFirstDifIndex(hashes, getMainVersion())
        } ?: 1
        return ChainAnswer.ANSWER.apply { data = mainBlocks.subList(firstDif, mainBlocks.size) }
    }

    fun findFirstDifIndex(hashes: List<String>, hashes2: List<String>) =
        (0 until min(hashes.size, hashes2.size))
            .firstOrNull { hashes[it]!=hashes2[it] } ?: hashes.lastIndex

    override fun answerData(answer: Any): ChainAnswer {

        (answer as? List<Block>)?.let {
            if (it.last().depth >= mainBlocks.last().depth) {

                mainBlocks.removeAll(mainBlocks.takeLast(mainBlocks.last().depth - it.first().depth + 1))
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