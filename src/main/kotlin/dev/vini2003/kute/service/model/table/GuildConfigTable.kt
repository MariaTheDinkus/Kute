package dev.vini2003.kute.service.model.table

import org.jetbrains.exposed.dao.id.LongIdTable

object GuildConfigTable : LongIdTable() {
	val prefix = char("prefix")
}