package com.anton22255.agent

import com.anton22255.Block
import com.anton22255.InitData
import com.anton22255.Statistic
import com.anton22255.Transaction
import com.anton22255.blockchain.AntBlockChain
import com.anton22255.blockchain.Chain
import com.anton22255.blockchain.ChainAnswer
import com.anton22255.transport.Message
import com.anton22255.transport.Type
import java.util.*
import kotlin.collections.ArrayList

class HonestAgent(
    override val hashRate: Long,
    blockChain: Chain,
    private val initData: InitData,
    private val statistic: Statistic
) : Agent {

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

    override suspend fun receiveMessage(message: Message) {
        //parse message

        when (val answer = processMessage(message)) {

            ChainAnswer.ACCEPT -> {

                var data: Any? = null
                var type: Type? = null
                when (answer.data) {
                    is Transaction -> {
                        data = (answer.data as Transaction).copy()
                        type = Type.TRANSACTION
                    }
                    is Block -> {
                        data = (answer.data as Block).copy()
                        type = Type.BLOCK
                    }
                    else ->
                        println("WTF ${answer.name}  ${answer.data} ${message.data} ${message.type}")
                }

                sendMessageToChannels(
                    data!!,
                    message.expiredTime,
                    type!!
                )
            }

            ChainAnswer.REQUEST -> {

                sendMessageToChannel(
                    type = Type.REQUEST,
                    data = answer.data,
                    time = message.expiredTime,
                    recipientId = message.senderId
                )
            }

            ChainAnswer.ANSWER -> {
                sendMessageToChannel(
                    type = Type.ANSWER,
                    data = answer.data,
                    time = message.expiredTime,
                    recipientId = message.senderId,
                    addedTime = initData.sendTime.plus(
                        (answer.data as? List<*>)?.size?.times(initData.sendBlockTime) ?: initData.sendTime.toDouble()
                    ).toInt()
                )
            }
        }
    }

    private fun processMessage(message: Message): ChainAnswer {

        synchronized(this) {

            return when (message.type) {
                Type.TRANSACTION -> {
                    val transaction = message.data as Transaction
                    if (transactionPool.contains(transaction)) {
                        transactionPool.add(transaction)

                        ChainAnswer.ACCEPT.apply {
                            data = message.data
                        }
                    } else {
                        ChainAnswer.DECLINE
                    }
                }
                Type.BLOCK -> {
                    blockChain.addBlock(message.data as Block).also {
//                    if (it == ChainAnswer.REQUEST) {
//                        println("in blockchain ChainAnswer.REQUEST")
//                    }
//                    if (blockChain is AntBlockChain && (blockChain as AntBlockChain).hasForkOnAction) {
//                        statistic.incrementForkCount(message.expiredTime.toInt())
//                    }
                    }
                }

                Type.REQUEST -> {
                    blockChain.requestData(message.data)
                }

                Type.ANSWER -> {
                    blockChain.answerData(message.data)
//                    .apply {
//                        if (this == ChainAnswer.ACCEPT) {
//                            statistic.incrementForkCount(message.expiredTime.toInt())
//                        }
//                    }
                }
                else -> {
                    ChainAnswer.DECLINE

                }
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
                val chainAnswer = blockChain.addBlock(it)
                if (chainAnswer == ChainAnswer.ACCEPT) {
                    sendMessageToChannels(it, time, Type.BLOCK)
                }
            }
    }

    private fun sendMessageToChannels(
        data: Any,
        time: Long,
        type: Type
    ) {
        channels.forEach { agent ->
            sendMessageToChannel(type, data, time, initData.sendTime, agent.id)
        }
    }

    private fun sendMessageToChannel(
        type: Type,
        data: Any,
        time: Long,
        addedTime: Int = initData.sendTime,
        recipientId: String
    ) {
        messagesForSending.add(
            Message(
                type = type,
                data = data,
                initTime = time,
                expiredTime = time + addedTime,
                senderId = id,
                recipientId = recipientId
            )
        )
    }
}