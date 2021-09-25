package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.audio.track.AudioTrackQueue
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.XEmoji
import dev.vini2003.kute.util.extension.ordinal
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val QueueCommand = Command { root ->
	val queue = literal<CommandSource>("queue") {
		execute(QueueCommandExecutor)
	}.build()
	
	val q = literal<CommandSource>("q") {
		execute(QueueCommandExecutor)
	}.build()
	
	root.addChild(queue)
	root.addChild(q)
}

val QueueCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val queue = AudioManager.Queue[guild.id] ?: return@launch
			
			if (queue.isNotEmpty()) {
				createQueueMessage(queue, message)
			} else {
				createEmptyQueueMessage(message)
			}
		}
	}
}

private fun createQueueMessage(queue: AudioTrackQueue, message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			queue.entries.take(16).mapIndexed { index, entry ->
				"\\\uD83C\uDFB5  **${(index + 1).ordinal()}** - ${entry.name}"
			}.joinToString("\n").apply {
				spec.setContent(this + if (queue.entries.size > 16) {
					"\n.**..and ${queue.entries.size - 16} more songs!**"
				} else {
					""
				})
			}
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