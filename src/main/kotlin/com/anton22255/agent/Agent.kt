package com.anton22255.agent

import com.anton22255.blockchain.Chain
import com.anton22255.transport.Message

interface Agent {

    val id: String
    val channels: Collection<Agent>
    val blockChain: Chain
    val hashRate: Long

    fun sendMessage(): List<Message>

    suspend fun receiveMessage(message: Message)

    fun addChanel(agent: Agent)

    fun deleteChannels(agents: List<Agent>)

    fun createBlock(time: Long)

    fun init()

    fun clearMessage()
}
