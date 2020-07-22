package com.anton22255

import com.anton22255.blockchain.Chain
import com.anton22255.blockchain.ChainAnswer
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import java.util.*
import kotlin.collections.ArrayList

interface Agent {

    val id: String
    val channels: Collection<Agent>
    val blockChain: Chain
    val hashRate: Int

    fun sendMessage(): List<Message>

    fun receiveMessage(message: Message)

    fun addChanel(agent: Agent)

    fun deleteChannels(agents: List<Agent>)

    fun createBlock(time: Long)

    fun init()

    fun clearMessage()

}

class HonestAgent(override val hashRate: Int, blockChain: Chain) : Agent {
    override val id: String = UUID.randomUUID().toString()
    override val channels = ArrayList<Agent>()
    override var blockChain: Chain = blockChain
    private var transactionPool = ArrayList<Transaction>()

    private var messagesForSending = arrayListOf<Message>()

    override fun init() {
        blockChain = channels.lastOrNull()!!.blockChain.copy()
    }

    override fun sendMessage(): List<Message> {
        return messagesForSending
    }

    override fun clearMessage() {
        messagesForSending.clear()
    }

    override fun receiveMessage(message: Message) {
        //parse message

        when (processMessage(message)) {
            ChainAnswer.ACCEPT -> messagesForSending.add(message)
        }

    }

    private fun processMessage(message: Message): ChainAnswer {

        return when (message.type) {
            Type.TRANSACTION -> {
                transactionPool.add(message.data as Transaction)
                ChainAnswer.ACCEPT
            }
            Type.BLOCK -> {
                blockChain.addBlock(message.data as Block)
            }
            else -> {
                ChainAnswer.DECLINE

            }
        }
    }

    override fun addChanel(agent: Agent) {
        channels.add(agent)
    }

    override fun deleteChannels(agents: List<Agent>) {
        channels.removeAll(agents)
    }

    override fun createBlock(time: Long) {

        val transactions = transactionPool.take(200)
            .plus(Transaction.createRewardTransaction(this))

        Block(
            transactions,
            blockChain.getLastBlock()
        )
            .let {
                blockChain.addBlock(it)
                sendBlockMessageToChannels(it, time)
            }
    }

    private fun sendBlockMessageToChannels(it: Block, time: Long) {
        channels.forEach { agent ->
            messagesForSending.add(
                Message(
                    type = Type.BLOCK,
                    data = it,
                    initTime = time,
                    expiredTime = time + Main.sendTime,
                    senderId = id,
                    recipientId = agent.id
                )
            )
        }
    }
}