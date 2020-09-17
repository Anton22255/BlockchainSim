package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Statistic

class AntBlockChain() : Chain {
    private val blockPool = arrayListOf<Block>()
    private val mainBlocks: ArrayList<HashMap<String, Block>> = ArrayList()
    private var endOfMainBranch: Block
    override lateinit var statistic: Statistic

//    var hasForkOnAction = false

    init {
        val genesisBlock = createGenesisBlock()
        mainBlocks.add(hashMapOf(genesisBlock.calculateHash() to genesisBlock) as java.util.HashMap<String, Block>)
        endOfMainBranch = mainBlocks[0].values.first()
    }

    override fun getLastBlock() = endOfMainBranch

    override fun addBlock(block: Block): ChainAnswer {

//        hasForkOnAction = false
        val result = addToTree(block)
        if (result == ChainAnswer.ACCEPT) {
            processPool(block)
            findMainBranch()
            result.data = block
        }
        return result
    }

    override fun copy(): Chain =
        AntBlockChain().apply {
            this.blockPool.addAll(this@AntBlockChain.blockPool.toList())

            this@AntBlockChain.mainBlocks.forEach {
                val list = HashMap(it.map { it.value.copy() }.map { it.calculateHash() to it }.toMap())
                this.mainBlocks.add(list)
            }

            this.endOfMainBranch = this@AntBlockChain.endOfMainBranch.copy()
        }

    override fun requestData(request: Any): ChainAnswer {
        TODO("not implemented for this type") //To change body of created functions use File | Settings | File Templates.
    }

    override fun answerData(answer: Any): ChainAnswer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun addToTree(block: Block): ChainAnswer {

        synchronized(this) {
            val depth = block.depth - 1
            val blocksOnDepth = getLevel(depth)
            return if (blocksOnDepth?.isNotEmpty() == true) {
                blocksOnDepth[block.prevHash]
                    ?.let {
                        addToMainTree(block)
                    }
                    ?: addToPool(block)
//                blocksOnDepth.find {
//                    it.calculateHash() == block.prevHash
//                }
//                    ?.let {
//                        addToMainTree(block)
//                    } ?: addToPool(block)
            } else {
                addToPool(block)
            }
        }
    }

    fun addToMainTree(block: Block): ChainAnswer {
        val blocks = getLevel(block.depth)
        return if (blocks?.containsKey(block.calculateHash()) == true) {
            ChainAnswer.DECLINE
        } else {
            blocks?.put(block.calculateHash(), block) ?: mainBlocks.add(hashMapOf(block.calculateHash() to block))

//            if (blocks?.size ?: 0 > 1 && blocks?.count { it.prevHash == block.prevHash } ?: 0 > 1) {
//                hasForkOnAction = true
//            }

            processPool(block)
            ChainAnswer.ACCEPT
        }
    }

    private fun getLevel(depth: Int) = mainBlocks.getOrNull(depth)

    fun addToPool(block: Block): ChainAnswer {
        return if (!blockPool.contains(block)) {
            blockPool.add(block)
            ChainAnswer.ACCEPT
        } else {
            ChainAnswer.DECLINE
        }
    }

    fun processPool(addedBlock: Block) {

        synchronized(this) {
            blockPool.filter {
                addedBlock.calculateHash() == it.prevHash
            }?.map {
                addToMainTree(it)
            }
        }
    }

    fun findMainBranch() {
        endOfMainBranch = mainBlocks.last().values.first()
    }

    override fun getMainVersion(): List<String> {

        val resultList = arrayListOf(endOfMainBranch)
        var prevBlock = endOfMainBranch.prevBlock
        while (prevBlock != null) {
            resultList.add(prevBlock)
            prevBlock = prevBlock.prevBlock

        }
        return resultList.reversed().map { block -> block.calculateHash() }
    }
}