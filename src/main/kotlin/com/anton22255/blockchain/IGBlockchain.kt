package com.anton22255.blockchain

import com.anton22255.Block

class IGBlockchain : Chain {
    private val mainBlocks: MutableList<Block> = arrayListOf(createGenesisBlock())

    override fun getLastBlock(): Block = mainBlocks.last()

    override fun addBlock(block: Block): ChainAnswer {

        return if (block.depth <= getLastBlock().depth) {
            ChainAnswer.DECLINE
        } else {
            if (block.prevHash == getLastBlock().calculateHash()) {
                mainBlocks.add(block)
                ChainAnswer.ACCEPT
            } else {
                ChainAnswer.REQUEST.apply {
                    data = mainBlocks.map { it.calculateHash() }.toList()
                }
            }
        }
    }

    override fun requestData(request: Any): Any {
        (request as? List<String>)?.let {

            //todo

        }
    }

    override fun copy(): Chain =
        IGBlockchain().apply {
            mainBlocks.clear()
            mainBlocks.addAll(mainBlocks.toMutableList())
        }
}