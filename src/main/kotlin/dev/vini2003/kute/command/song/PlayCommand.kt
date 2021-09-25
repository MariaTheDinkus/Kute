package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.external.youtube.YoutubeClient
import dev.vini2003.kute.manager.QueueManager
import dev.vini2003.kute.audio.song.SongRequest
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.util.*
import dev.vini2003.kute.util.extension.orNull
import dev.vini2003.kute.util.extension.sha256
import dev.vini2003.kute.util.extension.startNextTrack
import dev.vini2003.kute.util.extension.toSequentialNumericString
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking

private val YoutubeVideoRegex = ".+youtube.com/watch\\?v=(.+)".toRegex()
private val ShortYoutubeVideoRegex = ".+youtu.be/(.+)".toRegex()
private val YoutubePlaylistRegex = ".+youtube.com/playlist\\?list=(.+)".toRegex()

private val SpotifyTrackRegex = ".+spotify.com/track/(.+)\\?.+".toRegex()
private val SpotifyPlaylistRegex = ".+spotify.com/playlist/(.+)\\?.+".toRegex()
private val SpotifyAlbumRegex = ".+spotify.com/album/(.+)\\?.+".toRegex()

val PlayCommand = Command { root ->
	val play = literal<CommandSource>("play") {
		greedyString("search") {
			execute(PlayCommandExecutor)
		}
	}.build()
	
	val p = literal<CommandSource>("p") {
		redirect(play)
	}.build()
	
	root.addChild(play)
	root.addChild(p)
}

val PlayCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingleOrNull() ?: return@launch
			val member = message.authorAsMember.awaitSingleOrNull() ?: return@launch
			val memberVoiceState = member.voiceState.awaitSingleOrNull() ?: return@launch
			val memberVoiceChannel = memberVoiceState.channel.awaitSingleOrNull() ?: return@launch
			
			AudioManager.load(source.message.guildId.orNull!!)
			
			memberVoiceChannel.join { spec ->
				val provider = AudioManager.Provider[guild.id]!!
				
				spec.setProvider(provider)
			}.awaitSingle()
			
			val search = getString("search")
			
			when {
				search.matches(YoutubeVideoRegex) -> {
					enqueueYouTubeVideoById(source, YoutubeVideoRegex.find(search)!!.groups[1]!!.value, message, member, guild)
					
					perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
				}
				
				search.matches(ShortYoutubeVideoRegex) -> {
					enqueueYouTubeVideoById(source, ShortYoutubeVideoRegex.find(search)!!.groups[1]!!.value, message, member, guild)
					
					perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
				}
				
				search.matches(YoutubePlaylistRegex) -> {
					enqueueYouTubePlaylistById(source, YoutubePlaylistRegex.find(search)!!.groups[1]!!.value, message, member, guild)
				}
				
				search.matches(SpotifyTrackRegex) -> {
					enqueueSpotifyTrackById(source, SpotifyTrackRegex.find(search)!!.groups[1]!!.value, message, member, guild)
					
					perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
				}
				
				search.matches(SpotifyPlaylistRegex) -> {
					enqueueSpotifyPlaylistById(source, SpotifyPlaylistRegex.find(search)!!.groups[1]!!.value, message, member, guild)
				}
				
				search.matches(SpotifyAlbumRegex) -> {
					enqueueSpotifyAlbumById(source, SpotifyAlbumRegex.find(search)!!.groups[1]!!.value, message, member, guild)
				}
				
				else -> {
					enqueueYouTubeVideoByName(source, search, message, member, guild)
					
					perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
				}
			}
		}
	}
}

private suspend fun enqueueYouTubeVideoByName(source: CommandSource, name: String, message: Message, member: Member, guild: Guild, silent: Boolean = false, trackId: String? = null) {
	val scrapedVideoId = getVideoIdByScrapedSearch(name)
	
	enqueueYouTubeVideoById(source, scrapedVideoId, message, member, guild, silent)
}

private suspend fun enqueueYouTubeVideoById(source: CommandSource, id: String, message: Message, member: Member, guild: Guild, silent: Boolean = false) {
	val list = source.youtube.getVideos {
		requestSnippet()
		
		filterId(id)
		
		maxResults(1)
	}
	
	if (list.items.isNotEmpty()) {
		val item = list.items.first()
		
		enqueue(item.snippet.title, item.snippet.channelTitle, item.id, item.snippet.thumbnails, message, member, guild)
		
		val queue = AudioManager.Queue[guild.id]!!
		val player = AudioManager.Player[guild.id]!!
		
		if (queue.hasNext() && player.playingTrack != null) {
			if (!silent) {
				createEnqueuedVideoMessage(item.snippet.title, item.snippet.channelTitle, item.id, item.snippet.thumbnails, message, member)
			}
		}
	} else {
		if (!silent) {
			createNoYouTubeVideoFoundMessage(message)
		}
	}
}

private suspend fun enqueueYouTubePlaylistById(source: CommandSource, id: String, message: Message, member: Member, guild: Guild) {
	val list = source.youtube.getPlaylists {
		requestSnippet()
		
		filterId(id)
		
		maxResults(1)
	}
	
	if (list.items.isNotEmpty()) {
		createEnqueuedPlaylistMessage(message, member, list.items.size)

		val first = list.items.first()
		
		enqueue(first.snippet.title, first.snippet.channelTitle, first.id, first.snippet.thumbnails, message, member, guild)
		
		perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
		
		list.items.subList(1, list.items.size - 1).forEach { item ->
			GlobalScope.launch {
				enqueue(item.snippet.title, item.snippet.channelTitle, item.id, item.snippet.thumbnails, message, member, guild)
			}
		}
	} else {
		createNoOrEmptyYouTubePlaylistFoundMessage(message)
	}
}

private suspend fun enqueueSpotifyTrackById(source: CommandSource, id: String, message: Message, member: Member, guild: Guild, silent: Boolean = false) {
	val track = source.spotify.getTrack(id).build().executeAsync().await()
	
	if (track != null) {
		enqueueYouTubeVideoByName(source, "${track.name} - ${track.artists.joinToString(" ") { artist -> "\"${artist.name}\"" }}", message, member, guild, silent, track.id)
	} else {
		createNoSpotifyTrackFoundMessage(message)
	}
}

private suspend fun enqueueSpotifyPlaylistById(source: CommandSource, id: String, message: Message, member: Member, guild: Guild) {
	val playlist = source.spotify.getPlaylist(id).build().executeAsync().await()
	
	if (playlist != null) {
		if (playlist.tracks.items.isNotEmpty()) {
			createEnqueuedPlaylistMessage(message, member, playlist.tracks.items.size)
			
			val first = playlist.tracks.items.first()
			
			enqueueSpotifyTrackById(source, first.track.id, message, member, guild, false)
			
			perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
			
			GlobalScope.launch {
				playlist.tracks.items.toList().subList(1, playlist.tracks.items.size - 1).forEach { item ->
					enqueueSpotifyTrackById(source, item.track.id, message, member, guild, true)
				}
			}
		} else {
			createNoOrEmptySpotifyPlaylistFoundMessage(message)
		}
	} else {
		createNoOrEmptySpotifyPlaylistFoundMessage(message)
	}
}

private suspend fun enqueueSpotifyAlbumById(source: CommandSource, id: String, message: Message, member: Member, guild: Guild) {
	val album = source.spotify.getAlbum(id).build().executeAsync().await()
	
	if (album != null) {
		if (album.tracks.items.isNotEmpty()) {
			createEnqueuedAlbumMessage(message, member, album.tracks.items.size)
			
			val first = album.tracks.items.first()
			
			enqueueSpotifyTrackById(source, first.id, message, member, guild, false)
			
			perhapsPlayNextTrack(AudioManager.Player[guild.id]!!)
			
			GlobalScope.launch {
				album.tracks.items.toList().subList(1, album.tracks.items.size - 1).forEach { item ->
					enqueueSpotifyTrackById(source, item.id, message, member, guild, true)
				}
			}
		} else {
			createNoOrEmptySpotifyAlbumFoundMessage(message)
		}
	} else {
		createNoOrEmptySpotifyAlbumFoundMessage(message)
	}
}

private fun perhapsPlayNextTrack(player: AudioPlayer) {
	val queue = AudioManager.Queue[AudioManager.Player[player]!!]!!
	
	if (queue.isNotEmpty() && player.playingTrack == null) {
		player.startNextTrack()
	}
}

private fun url(id: String) = "https://youtube.com/watch?v=${id}"

private fun request(title: String, channel: String, id: String, thumbnails: Map<String, YoutubeClient.Thumbnail>, message: Message, member: Member): SongRequest {
	return SongRequest(title, channel, url(id), thumbnails["default"]!!.url, message, member)
}

private fun enqueue(title: String, channel: String, id: String, thumbnails: Map<String, YoutubeClient.Thumbnail>, message: Message, member: Member, guild: Guild) {
	QueueManager.enqueue(guild, request(title, channel, id, thumbnails, message, member))
}

private fun createEnqueuedVideoMessage(title: String, channel: String, id: String, thumbnails: Map<String, YoutubeClient.Thumbnail>, message: Message, member: Member) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createEmbed { spec ->
			spec.setAuthor("Enqueued...", url(id), member.avatarUrl)
			spec.addField("Song", title, true)
			spec.addField("Author", channel, true)
			spec.setThumbnail(thumbnails["default"]!!.url)
		}.subscribe()
	}
}

private fun createEnqueuedPlaylistMessage(message: Message, member: Member, amount: Int) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createEmbed { spec ->
			spec.setAuthor("Enqueued ${amount.toSequentialNumericString()} song${if (amount > 1) "s" else ""} from a playlist...", null, member.avatarUrl)
		}.subscribe()
	}
}

private fun createEnqueuedAlbumMessage(message: Message, member: Member, amount: Int) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createEmbed { spec ->
			spec.setAuthor("Enqueued ${amount.toSequentialNumericString()} song${if (amount > 1) "s" else ""} from an album...", null, member.avatarUrl)
		}.subscribe()
	}
}

private fun createNoYouTubeVideoFoundMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$YoutubeEmoji **Video not found.**")
		}.subscribe()
	}
}

private fun createNoOrEmptyYouTubePlaylistFoundMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$YoutubeEmoji **Playlist not found.**")
		}.subscribe()
	}
}

private fun createNoSpotifyTrackFoundMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$SpotifyEmoji **Track not found.**")
		}.subscribe()
	}
}

private fun createNoOrEmptySpotifyPlaylistFoundMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$SpotifyEmoji **Playlist not found.**")
		}.subscribe()
	}
}

private fun createNoOrEmptySpotifyAlbumFoundMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$SpotifyEmoji **Album not found.**")
		}.subscribe()
	}
}