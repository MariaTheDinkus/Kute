package dev.vini2003.kute.command.configuration

import com.mojang.brigadier.context.CommandContext
import dev.vini2003.kute.command.Command
import dev.vini2003.kute.external.brigadier.*
import dev.vini2003.kute.service.model.table.GuildConfigTable
import dev.vini2003.kute.service.model.table.entity.GuildConfig
import dev.vini2003.kute.util.contains
import dev.vini2003.kute.util.create
import dev.vini2003.kute.util.first
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction

val PrefixCommand = Command { root ->
	val prefix = literal<CommandSource>("prefix") {
		greedyString("prefix") {
			execute(PrefixCommandExecutor)
		}
	}.build()
	
	root.addChild(prefix)
}

val PrefixCommandExecutor: CommandContext<CommandSource>.() -> Unit = {
	runBlocking {
		launch {
			val message = source.message
			val guild = message.guild.awaitSingle()
			
			val prefix = getString("prefix")
			
			if (prefix.length < 1) {
				createPrefixTooShortMessage(message)
			} else if (prefix.length > 1) {
				createPrefixTooLongMessage(message)
			} else {
				createPrefixMessage(message, prefix.first())
				
				if (GuildConfig.contains(GuildConfigTable.id, guild.id.asLong())) {
					val existing = GuildConfig.first(GuildConfigTable.id, guild.id.asLong())
					
					transaction {
						existing.prefix = prefix.first()
					}
				} else {
					GuildConfig.create(guild.id.asLong()) {
						this.prefix = prefix.first()
					}
				}
			}
		}
	}
}

private fun createPrefixTooLongMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("**Prefix too long.**")
		}.subscribe()
	}
}

private fun createPrefixTooShortMessage(message: Message) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("**Prefix too short.**")
		}.subscribe()
	}
}

private fun createPrefixMessage(message: Message, prefix: Char) {
	message.channel.subscribe { messageChannel ->
		messageChannel.createMessage { spec ->
			spec.setContent("**Prefix set to `$prefix`.**")
		}.subscribe()
	}
}