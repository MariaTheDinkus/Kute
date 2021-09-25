package dev.vini2003.kute.service.model.table.column

import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Table

class InstantColumnType(private val longColumn: LongColumnType = LongColumnType()) : IColumnType by longColumn {
	override fun valueFromDB(value: Any): Instant {
		if (value is Instant) return value
		if (value is Long) return Instant.fromEpochMilliseconds(value)
		throw UnsupportedOperationException()
	}
	
	override fun valueToDB(value: Any?): Long {
		return (value as Instant).toEpochMilliseconds()
	}
}

fun Table.instant(name: String): Column<Instant> = registerColumn(name, InstantColumnType())