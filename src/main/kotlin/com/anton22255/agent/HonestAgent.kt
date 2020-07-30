package com.anton22255.agent

import com.anton22255.Block
import com.anton22255.InitData
import com.anton22255.Statistic
import com.anton22255.Transaction
import com.anton22255.blockchain.Chain
import com.anton22255.blockchain.ChainAnswer
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import java.util.*
import kotlin.collections.ArrayList

class HonestAgent(
    override val hashRate: Long,
    blockChain: Chain,
    val initData: InitData,
    val statistic: Statistic
) :
    Agent {
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

        val chainAnswer = processMessage(message)
        when (Pair(message.type, chainAnswer)) {
            Type.TRANSACTION, ChainAnswer.ACCEPT -> {
                sendMessageToChannels(
                    (message.data as Transaction).copy(),
                    message.expiredTime,
                    message.type
                )
            }
            Type.BLOCK, ChainAnswer.ACCEPT -> {
                sendMessageToChannels(
                    (message.data as Block).copy(),
                    message.expiredTime,
                    message.type
                )
            }

            ChainAnswer.REQUEST -> messagesForSending.add(
                Message(
                    Type.REQUEST,
                    chainAnswer.data,
                    id,
                    message.senderId,
                    message.expiredTime,
                    message.expiredTime + initData.sendTime
                )
            )

            ChainAnswer.ANSWER -> sendMessage(
                chainAnswer,
                message,
                Type.ANSWER,
                (initData.sendTime + initData.sendBlockTime * ((chainAnswer.data as? List<*>)?.size?.toDouble() ?: 0.0)).toInt()
            )
        }

    }

    private fun sendMessage(
        chainAnswer: ChainAnswer,
        message: Message,
        type: Type,
        extraTime: Int
    ) {
        messagesForSending.add(
            Message(
                type,
                chainAnswer.data,
                id,
                message.senderId,
                message.expiredTime,
                message.expiredTime + extraTime
            )
        )
    }

    private fun processMessage(message: Message): ChainAnswer {

        return when (message.type) {
            Type.TRANSACTION -> {
                transactionPool.add(message.data as Transaction)
                ChainAnswer.ACCEPT.apply {
                    data = message.data
                }
            }
            Type.BLOCK -> {
                blockChain.addBlock(message.data as Block).also {
                    if (it == ChainAnswer.DECLINE && message.data.depth == blockChain.getLastBlock().depth) {
                        statistic.incrementForkCount(message.expiredTime.toInt())
                    }
                }
            }

            Type.REQUEST -> {
                blockChain.requestData(message.data)
            }

            Type.ANSWER -> {
                blockChain.answerData(message.data)
                    .also {
                        if (it == ChainAnswer.ACCEPT) {
                            statistic.incrementForkCount(message.expiredTime.toInt())
                        }
                    }
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
                sendMessageToChannels(it, time, Type.BLOCK)
            }
    }

    private fun sendMessageToChannels(
        it: Any,
        time: Long,
        type: Type
    ) {
        channels.forEach { agent ->
            messagesForSending.add(
                Message(
                    type = type,
                    data = it,
                    initTime = time,
                    expiredTime = time + initData.sendTime,
                    senderId = id,
                    recipientId = agent.id
                )
            )
        }
    }
}