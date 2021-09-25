package dev.vini2003.kute.service.model.table.entity

import dev.vini2003.kute.service.model.table.GuildConfigTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildConfig(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<GuildConfig>(GuildConfigTable)
	
	var prefix by GuildConfigTable.prefix
}