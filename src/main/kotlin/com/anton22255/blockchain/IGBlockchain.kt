package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Statistic
import kotlin.math.min

class IGBlockchain : Chain {

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

    override fun requestData(request: Any): ChainAnswer =
        (request as? List<String>)?.let { hashes ->

            val firstDif = (0 until min(hashes.size, mainBlocks.size))
                .first { hashes[it] != mainBlocks[it].calculateHash() }
            return ChainAnswer.ANSWER.apply { mainBlocks.subList(firstDif, mainBlocks.lastIndex) }
        }
            ?: ChainAnswer.ANSWER.apply { mainBlocks.subList(1, mainBlocks.lastIndex) }

    override fun answerData(answer: Any): ChainAnswer {

        (answer as? List<Block>)?.let {
            if (it.last().depth > mainBlocks.last().depth) {

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

    override fun copy(): Chain =
        IGBlockchain().apply {
            mainBlocks.clear()
            mainBlocks.addAll(mainBlocks.toMutableList())
        }

}