package com.anton22255.blockchain

import com.anton22255.Block
import com.anton22255.Transaction

data class BlockChain(val blocks: MutableList<Block> = arrayListOf(createGenesisBlock()))

