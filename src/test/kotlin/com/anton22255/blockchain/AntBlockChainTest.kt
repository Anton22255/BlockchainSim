package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Transaction
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

internal class AntBlockChainTest {

    lateinit var blockChain: AntBlockChain

    @Before
    fun initChain() {
        blockChain = AntBlockChain()
    }

    @Test
    fun testAdd() {

        val lastBlock = blockChain.getLastBlock()
        val block: Block = Block(arrayListOf(Transaction()), lastBlock)

        val addingResult = blockChain.addBlock(block)
        assertEquals(ChainAnswer.ACCEPT, addingResult)
        assertEquals(ChainAnswer.DECLINE, blockChain.addBlock(block))
    }

    @Test
    fun testAddToPool() {

        val block = Block(arrayListOf(Transaction()), null)

        var addingResult = blockChain.addToPool(block)
        assertEquals(ChainAnswer.ACCEPT, addingResult)

        addingResult = blockChain.addToPool(block)
        assertEquals(ChainAnswer.DECLINE, addingResult)

    }

    @Test
    fun testAddToMainTree() {

        val block = Block(arrayListOf(Transaction()), null)

        var addingResult = blockChain.addToPool(block)
        assertEquals(ChainAnswer.ACCEPT, addingResult)

        addingResult = blockChain.addToPool(block)
        assertEquals(ChainAnswer.DECLINE, addingResult)
    }

    @Test
    fun testGetBlock() {
        val lastBlock = blockChain.getLastBlock()
        val block = Block(arrayListOf(Transaction()), lastBlock)

        assertEquals((lastBlock.depth + 1), block.depth)
    }

    @Test
    fun testProcessPool() {

        val lastBlock = blockChain.getLastBlock()
        val nextBlock = Block(arrayListOf(Transaction()), lastBlock)
        val nextBlock2 = Block(arrayListOf(Transaction()), nextBlock)

        val answer = blockChain.addBlock(nextBlock2)
        assertEquals(ChainAnswer.ACCEPT, answer)
        assertEquals(lastBlock, blockChain.getLastBlock())

        val answer2 = blockChain.addBlock(nextBlock)
        assertEquals(ChainAnswer.ACCEPT, answer2)
        assertEquals(nextBlock2, blockChain.getLastBlock())
    }
}