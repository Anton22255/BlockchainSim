package com.anton22255.utils

import java.security.MessageDigest

object StringUtil {
    //Applies Sha256 to a string and returns the result.
    fun applySha256(input: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            //Applies sha256 to our input,
            val hash = digest.digest(input.toByteArray(charset("UTF-8")))
            val hexString = StringBuffer() // This will contain hash as hexidecimal
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}