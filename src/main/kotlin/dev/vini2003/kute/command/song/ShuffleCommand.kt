package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.DiceEmoji
import dev.vini2003.kute.util.XEmoji
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val ShuffleCommand = Command { root ->
	val shuffle = literal<CommandSource>("shuffle") {
		execute(ShuffleCommandExecutor)
	}.build()
	
	root.addChild(shuffle)
}

val ShuffleCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val queue = AudioManager.Queue[guild.id] ?: return@launch
			
			if (queue.isNotEmpty()) {
				queue.entries.shuffle()
				
				createShuffledMessage(message)
			} else {
				createEmptyQueueMessage(message)
			}
		}
	}
}

private fun createShuffledMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$DiceEmoji **Shuffled the song queue!**")
		}.subscribe()
	}
}

private fun createEmptyQueueMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$XEmoji **The song queue is empty!**")
		}.subscribe()
	}
}