package dev.vini2003.kute.util.extension

import discord4j.common.util.Snowflake

fun String.toSnowflake(): Snowflake {
	return if (isSnowflake()) {
		Snowflake.of(this.toLong())
	} else {
		throw IllegalArgumentException("string is not a Snowflake")
	}
}

fun String.isSnowflake(): Boolean {
	return try {
		this.toLong()
		true
	} catch (_: Exception) {
		false
	}
}