package com.anton22255.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.jdbc.PgArray
import java.sql.PreparedStatement

fun Table.arrayOfLong(name: String): Column<List<Long>> =
    registerColumn(name, LongArrayColumnType())

internal class LongArrayColumnType : ColumnType() {
    override fun sqlType() = "BIGINT[]"
    override fun valueFromDB(value: Any): List<Long> = when (value) {
        is Iterable<*> -> value.map { it.toString().toLong() }
        is PgArray -> {
            val array = value.array
            if (array is Array<*>) {
                array.map {
                    when (it) {
                        is Long -> it
                        is Number -> it.toLong()
                        null -> error("Unexpected value of type Long but value is $it")
                        else -> error("Unexpected value of type Long: $it of ${it::class.qualifiedName}")
                    }
                }
            } else {
                throw Exception("Values returned from database if not of type kotlin Array<*>")
            }
        }
        else -> throw Exception("Values returned from database if not of type PgArray")
    }

    override fun valueToString(value: Any?): String = when (value) {
        null -> {
            if (!nullable) error("NULL in non-nullable column")
            "NULL"
        }

        is Iterable<*> -> {
            "'{${value.joinToString()}}'"
        }

        else -> {
            nonNullValueToString(value)
        }
    }

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        if (value is List<*>) {
            stmt.setArray(index, stmt.connection.createArrayOf("bigint", value.toTypedArray()))
        } else {
            super.setParameter(stmt, index, value)
        }
    }
}