package dev.vini2003.kute.service.model.table.column

import discord4j.common.util.Snowflake
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Table

class SnowflakeColumnType(private val longColumn: LongColumnType = LongColumnType()) : IColumnType by longColumn {
	override fun valueFromDB(value: Any): Snowflake {
		if (value is Snowflake) return value
		else return Snowflake.of(super.valueFromDB(value) as Long)
	}
	
	override fun valueToDB(value: Any?): Long {
		return (value as Snowflake).asLong()
	}
}

fun Table.snowflake(name: String): Column<Snowflake> = registerColumn(name, SnowflakeColumnType())