package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Transaction
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test


internal class IGBlockchainTest {

    lateinit var blockChain: IGBlockchain

    @Before
    fun initChain() {
        blockChain = IGBlockchain()
    }

    @Test
    fun answerData() {

        val lastBlock = blockChain.getLastBlock()
        val block1 = Block(arrayListOf(Transaction()), lastBlock)
        val block2 = Block(arrayListOf(Transaction()), block1)
        val block3 = Block(arrayListOf(Transaction()), block2)

        blockChain.addBlock(block1)
        blockChain.addBlock(block2)
        blockChain.addBlock(block3)

        val block2_1 = Block(arrayListOf(Transaction()), block1)
        val block3_1 = Block(arrayListOf(Transaction()), block2_1)
        val block4_1 = Block(arrayListOf(Transaction()), block3_1)

        val answerData = blockChain.answerData(listOf(block2_1, block3_1, block4_1))

        assertEquals(ChainAnswer.ACCEPT.name, answerData.name)

        val expectedBlocks = arrayListOf(
            lastBlock,
            block1,
            block2_1,
            block3_1,
            block4_1
        ).map { it.calculateHash() }

        assertEquals(
            expectedBlocks,
            blockChain.mainBlocks.map { it.calculateHash() })


    }
}