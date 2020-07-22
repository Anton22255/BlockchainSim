package com.anton22255.blockchain

import com.anton22255.Block

data class BlockChain(val blocks: MutableList<Block> = arrayListOf(createGenesisBlock()))

