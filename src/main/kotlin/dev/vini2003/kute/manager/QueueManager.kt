package dev.vini2003.kute.manager

import dev.vini2003.kute.audio.song.SongRequest
import discord4j.core.`object`.entity.Guild

object QueueManager {
	fun enqueue(guild: Guild, songRequest: SongRequest) {
		AudioManager.Queue[guild.id]?.plusAssign(songRequest)
	}
	
	fun dequeue(guild: Guild, songRequest: SongRequest) {
		AudioManager.Queue[guild.id]?.plusAssign(songRequest)
	}
}

