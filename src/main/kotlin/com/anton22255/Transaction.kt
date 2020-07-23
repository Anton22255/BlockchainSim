package com.anton22255

import com.anton22255.agent.Agent
import java.util.Arrays

data class Transaction(
    private val originator: String = "Satoshi",
    private var announcer: String? = null,
    private val content: String = "GENESIS",
    private val signature: ByteArray = ByteArray(0)
) {

    private var id: Long

    init {
        this.id = counter++
    }

    override fun equals(obj: Any?): Boolean {
        return id == (obj as? Transaction)?.id
    }

    override fun hashCode(): Int {
        var result = originator.hashCode()
        result = 31 * result + (announcer?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + Arrays.hashCode(signature)
        result = 31 * result + id.hashCode()
        return result
    }

    companion object {
        private var counter: Long = 1

        fun createRewardTransaction(agent: Agent) =
            Transaction(
                originator = agent.id,
                announcer = agent.id,
                content = "${agent.id}  gets reward 10$",
                signature = agent.id.toByteArray()
            )
    }
}
