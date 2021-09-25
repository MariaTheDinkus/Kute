package dev.vini2003.kute.util.extension

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.vini2003.kute.manager.AudioManager

fun AudioPlayer.startNextTrack() {
	val id = AudioManager.Player[this] ?: return
	
	val queue = AudioManager.Queue[id] ?: return
	
	val scheduler = AudioManager.Scheduler[id] ?: return
	
	val next = queue.next() ?: return
	
	next.message.channel.subscribe { channel ->
		channel.createEmbed { spec ->
			spec.setAuthor("Now playing...", next.url, next.message.author.orNull!!.avatarUrl)
			spec.addField("Song", next.name, true)
			spec.addField("Author", next.author, true)
			spec.setThumbnail(next.thumbnailUrl)
		}.subscribe()
	}
	
	AudioManager.loadItem(next.url, scheduler)
}