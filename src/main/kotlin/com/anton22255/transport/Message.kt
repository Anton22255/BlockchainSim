package com.anton22255.transport

import sun.management.Agent

data class Message(
    val type: Type,
    val data: Any,
    val senderId: String,
    val recipientId: String,
    val initTime: Long,
    val expiredTime: Long
)

enum class Type {
    TRANSACTION,
    BLOCK,
    SETTING,
    REQUEST,
    ANSWER
}



