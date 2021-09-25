package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.FastForwardEmoji
import dev.vini2003.kute.util.XEmoji
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val SkipCommand = Command { root ->
	val skip = literal<CommandSource>("skip") {
		execute(SkipCommandExecutor)
	}.build()
	
	val s = literal<CommandSource>("s") {
		execute(SkipCommandExecutor)
	}.build()
	
	root.addChild(skip)
	root.addChild(s)
}

private val SkipCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val player = AudioManager.Player[guild.id] ?: return@launch
			
			if (player.playingTrack != null) {
				createSkippedSongMessage(message)
				
				player.stopTrack()
			} else {
				createNoSongMessage(message)
			}
		}
	}
}

private fun createSkippedSongMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$FastForwardEmoji **Skipped current song!**")
		}.subscribe()
	}
}

private fun createNoSongMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$XEmoji **There is no song being played!**")
		}.subscribe()
	}
}