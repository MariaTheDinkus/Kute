package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.WastebasketEmoji
import dev.vini2003.kute.util.XEmoji
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val ClearCommand = Command { root ->
	val clear = literal<CommandSource>("clear") {
		execute(ClearCommandExecutor)
	}.build()
	
	val c = literal<CommandSource>("c") {
		execute(ClearCommandExecutor)
	}.build()
	
	root.addChild(clear)
	root.addChild(c)
}

val ClearCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val queue = AudioManager.Queue[guild.id] ?: return@launch
			
			if (queue.isNotEmpty()) {
				queue.clear()
				
				createClearedQueueMessage(message)
			} else {
				createEmptyQueueMessage(message)
			}
		}
	}
}

private fun createClearedQueueMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$WastebasketEmoji **Cleared song queue!**")
		}.subscribe()
	}
}

private fun createEmptyQueueMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$XEmoji **Song queue is empty!**")
		}.subscribe()
	}
}