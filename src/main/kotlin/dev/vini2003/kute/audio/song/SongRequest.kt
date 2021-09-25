package dev.vini2003.kute.audio.song

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message

data class SongRequest(
	val name: String,
	val author: String,
	val url: String,
	val thumbnailUrl: String,
	val message: Message,
	val member: Member
)