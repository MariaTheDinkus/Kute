package dev.vini2003.kute.audio.track

import dev.vini2003.kute.audio.song.SongRequest

class AudioTrackQueue {
	val entries: MutableList<SongRequest> = mutableListOf()
	
	operator fun plusAssign(songRequest: SongRequest) {
		entries += songRequest
	}
	
	operator fun minusAssign(songRequest: SongRequest) {
		entries -= songRequest
	}
	
	fun clear() = entries.clear()
	
	fun isEmpty() = entries.isEmpty()
	
	fun isNotEmpty() = entries.isNotEmpty()
	
	fun next() = entries.removeFirstOrNull()
	
	fun hasNext() = entries.isNotEmpty()
}