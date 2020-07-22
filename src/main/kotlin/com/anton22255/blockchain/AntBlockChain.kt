package com.anton22255.blockchain

import com.anton22255.Block

class AntBlockChain : Chain {
    private val blockPool = arrayListOf<Block>()
    private val mainBlocks: ArrayList<MutableList<Block>> = ArrayList()
    private var endOfMainBranch: Block

    init {
        mainBlocks.add(arrayListOf(createGenesisBlock()))
        endOfMainBranch = mainBlocks[0][0]
    }

    override fun getLastBlock() = endOfMainBranch

    override fun addBlock(block: Block): ChainAnswer {

        val result = addToTree(block)
        if (result == ChainAnswer.ACCEPT) {
            processPool(block)
            findMainBranch()
        }
        return result
    }

    override fun copy(): Chain =
        AntBlockChain().apply {
            this.blockPool.addAll(this@AntBlockChain.blockPool.toList())

            this@AntBlockChain.mainBlocks.forEach {
                val list = it.map { it.copy() }.toMutableList()
                this.mainBlocks.add(list)
            }

            this.endOfMainBranch = this@AntBlockChain.endOfMainBranch.copy()
        }

    fun addToTree(block: Block): ChainAnswer {

        val depth = block.depth - 1
        val blocksOnDepth = mainBlocks.getOrNull(depth)
        return if (blocksOnDepth?.isNotEmpty() == true) {
            blocksOnDepth.find {
                it.calculateHash() == block.prevHash
            }
                ?.let {
                    addToMainTree(block)
                } ?: addToPool(block)
        } else {
            addToPool(block)
        }
    }

    fun addToMainTree(block: Block): ChainAnswer {
        val blocks = mainBlocks.getOrNull(block.depth)
        return if (blocks?.contains(block) == true) {
            ChainAnswer.DECLINE
        } else {
            blocks?.add(block) ?: mainBlocks.add(arrayListOf(block))
            processPool(block)
            ChainAnswer.ACCEPT
        }
    }

    fun addToPool(block: Block): ChainAnswer {
        return if (!blockPool.contains(block)) {
            blockPool.add(block)
            ChainAnswer.ACCEPT
        } else {
            ChainAnswer.DECLINE
        }
    }

    fun processPool(addedBlock: Block) {

        blockPool.filter {
            addedBlock.calculateHash() == it.prevHash
        }?.map {
            addToMainTree(it)
        }
    }

    fun findMainBranch() {
        endOfMainBranch = mainBlocks.last().first()
    }
}