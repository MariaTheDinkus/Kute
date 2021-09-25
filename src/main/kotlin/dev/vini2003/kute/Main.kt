/**
 * The MIT License
 *
 * Copyright (c) 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import com.mojang.brigadier.CommandDispatcher
import com.wrapper.spotify.SpotifyApi
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.vini2003.kute.command.configuration.PrefixCommand
import dev.vini2003.kute.external.brigadier.CommandSource
import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import dev.vini2003.kute.external.youtube.YoutubeClient
import dev.vini2003.kute.util.environmentVariable
import dev.vini2003.kute.util.extension.orNull
import dev.vini2003.kute.command.song.*
import dev.vini2003.kute.service.model.table.GuildConfigTable
import dev.vini2003.kute.service.model.table.entity.GuildConfig
import dev.vini2003.kute.util.firstOrNull
import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.mono
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val YoutubeApiKey = environmentVariable("YOUTUBE_API_KEY")

private val DiscordToken = environmentVariable("DISCORD_TOKEN")

private val SpotifyClientId = environmentVariable("SPOTIFY_CLIENT_ID")
private val SpotifyClientSecret = environmentVariable("SPOTIFY_CLIENT_SECRET")

private val DatabaseUrl = environmentVariable("DATABASE_URL")

private val PoolConfig = HikariConfig().apply {
	jdbcUrl = DatabaseUrl
	
	driverClassName = "org.h2.Driver"
}

private val PoolSource = HikariDataSource(PoolConfig).apply {
	Database.connect(this)
	
	transaction {
		SchemaUtils.createMissingTablesAndColumns(GuildConfigTable)
	}
}

private lateinit var Youtube: YoutubeClient
private lateinit var Spotify: SpotifyApi

fun main(args: Array<String>) {
	Youtube = createYoutube(YoutubeApiKey)
	Spotify = createSpotify(SpotifyClientId, SpotifyClientSecret)
	
	runDiscord(DiscordToken)
}

fun getOrCreatePrefix(guildId: Snowflake, default: Char = '!'): Char {
	val existing = GuildConfig.firstOrNull(GuildConfigTable.id, guildId.asLong())
	
	return existing?.prefix ?: default
}

fun createYoutube(apiKey: String): YoutubeClient {
	return YoutubeClient(apiKey)
}

fun createSpotify(clientId: String, clientSecret: String): SpotifyApi {
	return SpotifyApi.builder().setClientId(clientId).setClientSecret(clientSecret).build().apply {
		accessToken = clientCredentials().build().execute().accessToken
	}
}

fun runDiscord(token: String) {
	DiscordClient.create(token).withGateway { discord ->
		mono {
			discord.on(MessageCreateEvent::class.java).subscribe { event ->
				try {
					val dispatcher = CommandDispatcher<CommandSource>()
					
					val message = event.message
					val messageContent = message.content
					
					val guild = event.guildId.orNull ?: return@subscribe
					
					val prefix = getOrCreatePrefix(guild)
					
					if (messageContent.startsWith(prefix)) {
						val root = dispatcher.root
						
						PlayCommand(root)
						PauseCommand(root)
						SkipCommand(root)
						ClearCommand(root)
						RepeatCommand(root)
						QueueCommand(root)
						ShuffleCommand(root)
						
						PrefixCommand(root)
						
						val start = messageContent.trimStart(prefix).substringBefore(' ')
						val lowercaseStart = start.toLowerCase()
						
						dispatcher.execute(messageContent.trimStart(prefix).replaceFirst(start, lowercaseStart), CommandSource(discord, Youtube, Spotify, event.message.author.orNull!!, event.message))
					}
				} catch (_: Exception) {
				
				}
			}
		}
	}.block()
}
