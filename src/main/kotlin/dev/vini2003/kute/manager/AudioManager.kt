package dev.vini2003.kute.manager

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.vini2003.kute.audio.track.AudioTrackQueue
import discord4j.common.util.Snowflake
import discord4j.voice.AudioProvider
import java.util.concurrent.ConcurrentHashMap
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import dev.vini2003.kute.audio.track.AudioTrackHandler
import dev.vini2003.kute.audio.track.AudioTrackScheduler
import dev.vini2003.kute.audio.track.AudioTrackProvider

object AudioManager : DefaultAudioPlayerManager() {
    init {
        AudioSourceManagers.registerRemoteSources(this)
        this.configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.HIGH
    }
    
    fun load(id: Snowflake) {
        if (Player[id] == null) {
            val player = createPlayer()
            val provider = AudioTrackProvider(player)
            val scheduler = AudioTrackScheduler(player)
            val queue = AudioTrackQueue()
            
            player.addListener(AudioTrackHandler())
    
            Player[id] = player
            Provider[id] = provider
            Scheduler[id] = scheduler
            Queue[id] = queue
    
            Repeat[id] = false
        }
    }

    object Player : ConcurrentHashMap<Snowflake, AudioPlayer>() {
        operator fun get(player: AudioPlayer): Snowflake? {
            return entries.firstOrNull { entry -> entry.value == player }?.key
        }
    }
    
    object Provider : ConcurrentHashMap<Snowflake, AudioProvider>()
    
    object Scheduler : ConcurrentHashMap<Snowflake, AudioTrackScheduler>()
    
    object Queue : ConcurrentHashMap<Snowflake, AudioTrackQueue>()
    
    object Repeat : ConcurrentHashMap<Snowflake, Boolean>()
}

