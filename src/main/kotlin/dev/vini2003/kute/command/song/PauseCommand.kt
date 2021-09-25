package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.RootCommandNode
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.PauseEmoji
import dev.vini2003.kute.util.XEmoji
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val PauseCommand = Command { root ->
	val pause = literal<CommandSource>("pause") {
		execute(PauseCommandExecutor)
	}.build()
	
	root.addChild(pause)
}

val PauseCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val player = AudioManager.Player[guild.id] ?: return@launch
			
			if (player.playingTrack != null) {
				player.isPaused = true
				
				createPausedSongMessage(message)
			} else {
				createNoSongMessage(message)
			}
		}
	}
}

private fun createPausedSongMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$PauseEmoji **Paused current song!**")
		}.subscribe()
	}
}

private fun createNoSongMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$XEmoji **No song being played!**")
		}.subscribe()
	}
}