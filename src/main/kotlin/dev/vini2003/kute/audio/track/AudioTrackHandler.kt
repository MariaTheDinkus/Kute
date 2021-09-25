package dev.vini2003.kute.audio.track

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.util.extension.startNextTrack

class AudioTrackHandler : AudioEventAdapter() {
	override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		val id = AudioManager.Player[player] ?: return
		
		val repeat = AudioManager.Repeat[id]
		
		if (repeat == true) {
			player.startTrack(track.makeClone(), false)
		} else {
			player.startNextTrack()
		}
	}
	
	override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
		player.startNextTrack()
	}
}