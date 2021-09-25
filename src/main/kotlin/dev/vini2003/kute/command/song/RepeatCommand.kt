package dev.vini2003.kute.command.song

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.manager.AudioManager
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.util.RepeatEmoji
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking

val RepeatCommand = Command { root ->
	val repeat = literal<CommandSource>("repeat") {
		execute(RepeatCommandExecutor)
	}.build()
	
	val r = literal<CommandSource>("r") {
		redirect(repeat)
	}.build()
	
	root.addChild(repeat)
	root.addChild(r)
}

private val RepeatCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val repeat = AudioManager.Repeat[guild.id] ?: false
			
			AudioManager.Repeat[guild.id] = !repeat
			
			if (repeat) {
				createStopRepeatMessage(message)
			} else {
				createStartRepeatMessage(message)
			}
		}
	}
}

private fun createStartRepeatMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$RepeatEmoji **Enabled song repetition!**")
		}.subscribe()
	}
}

private fun createStopRepeatMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("$RepeatEmoji **Disabled song repetition!**")
		}.subscribe()
	}
}