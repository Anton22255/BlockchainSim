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
    fun addBlock() {
        val lastBlock = blockChain.getLastBlock()
        val block1 = Block(arrayListOf(Transaction()), lastBlock)
        val block2 = Block(arrayListOf(Transaction()), block1)
        val block3 = Block(arrayListOf(Transaction()), block2)

        blockChain.addBlock(block1)
        blockChain.addBlock(block2)
//        blockChain.addBlock(block3)

        val block2_1 = Block(arrayListOf(Transaction()), block1)
        val block3_1 = Block(arrayListOf(Transaction()), block2_1)
        val block4_1 = Block(arrayListOf(Transaction()), block3_1)


        val chainAnswer = blockChain.addBlock(block4_1)
        assertEquals(ChainAnswer.REQUEST.name, chainAnswer.name)

        val result = (chainAnswer.data as ArrayList<String>)
        assertEquals(
            result,
            blockChain.mainBlocks.map { it.calculateHash() }
        )
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

    @Test
    fun fullTest() {

        val lastBlock = blockChain.getLastBlock()
        val block1 = Block(arrayListOf(Transaction()), lastBlock)
        blockChain.addBlock(block1)
        val block2 = Block(arrayListOf(Transaction()), block1)
        blockChain.addBlock(block2)
//        val block3 = Block(arrayListOf(Transaction()), block2)
//        blockChain.addBlock(block3)

        val secondChain = IGBlockchain()
        secondChain.addBlock(block1)
        val block2_1 = Block(arrayListOf(Transaction(content = "block2_1")), block1)
        secondChain.addBlock(block2_1)
        val block3_1 = Block(arrayListOf(Transaction(content = "block3_1")), block2_1)
        secondChain.addBlock(block3_1)
        val block4_1 = Block(arrayListOf(Transaction(content = "block4_1")), block3_1)
        secondChain.addBlock(block4_1)

        val chainAnswer = blockChain.addBlock(block4_1)
        assertEquals(ChainAnswer.REQUEST.name, chainAnswer.name)

        val expectedBlocks1 = arrayListOf(
            lastBlock,
            block1,
            block2
        ).map { it.calculateHash() }

        assertEquals(
            expectedBlocks1,
            chainAnswer.data as List<String>
        )


        val requestData = secondChain.requestData(chainAnswer.data)

        assertBlockEquals(
            requestData, arrayListOf(
                block2_1,
                block3_1,
                block4_1
            )
        )


        val answerData = blockChain.answerData(requestData.data)
        assertEquals(ChainAnswer.ACCEPT.name, answerData.name)

        assertEquals(block4_1, answerData.data)

        assertEquals(
            blockChain.mainBlocks, arrayListOf(
                lastBlock,
                block1,
                block2_1,
                block3_1,
                block4_1
            )
        )
    }

    private fun assertBlockEquals(
        requestData: ChainAnswer,
        expectedBlocks: ArrayList<Block>
    ) {

        assertEquals(
            expectedBlocks,
            requestData.data as List<Block>
        )
    }


    @Test
    fun findDiff() {

        val a = listOf("a", "b", "c", "d", "qw")
        val b = listOf("a", "b", "f", "g")
        val findFirstDifIndex = blockChain.findFirstDifIndex(a, b)
        assertEquals(2, findFirstDifIndex)

        assertEquals(
            listOf("c", "d", "qw"),
            a.subList(fromIndex = findFirstDifIndex, toIndex = a.size)
        )


    }
}