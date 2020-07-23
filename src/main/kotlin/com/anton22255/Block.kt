package com.anton22255

import com.anton22255.utils.StringUtil

data class Block(var transactions: List<Transaction>, var prevBlock: Block? = null) {

    //header
    var prevHash: String? = null
    var rootMerkle: String? = null
    var timeStamp: Long = 0
    var targetDif: String? = null
    var nonce: String = ""
    val depth: Int

    init {
        this.prevHash = prevBlock?.calculateHash()
        depth = prevBlock?.depth?.plus(1) ?: 0
    }

    override fun equals(obj: Any?): Boolean {
        val block = obj as Block? ?: return false
        return this.calculateHash() == block.calculateHash()
    }

    fun calculateHash(): String {
        return StringUtil.applySha256(
            prevHash +
                    timeStamp.toString() +
                    transactions.toString()
        )
    }

    override fun toString(): String {
        return "Block(prevHash=$prevHash, hash=${calculateHash()}, depth=$depth, transactions=$transactions, prevBlock=$prevBlock, rootMerkle=$rootMerkle, timeStamp=$timeStamp, targetDif=$targetDif, nonce='$nonce')"
    }

}

