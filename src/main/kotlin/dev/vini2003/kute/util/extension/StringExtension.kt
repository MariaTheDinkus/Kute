package dev.vini2003.kute.util.extension

import io.ktor.http.*
import java.security.MessageDigest
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun String.toDuration() =
	runCatching {
		when {
			endsWith("s") -> Duration.seconds(substringBeforeLast("s").toInt())
			endsWith("m") -> Duration.minutes(substringBeforeLast("m").toInt())
			endsWith("h") -> Duration.hours(substringBeforeLast("h").toInt())
			endsWith("d") -> Duration.days(substringBeforeLast("d").toInt())
			endsWith("w") -> Duration.days(substringBeforeLast("w").toInt() * 7)
			endsWith("m") -> Duration.days(substringBeforeLast("m").toInt() * 30)
			endsWith("y") -> Duration.days(substringBeforeLast("y").toInt() * 365)
			else -> null
		}
	}.getOrNull()

fun String.toUrl() = Url(this)

fun String.md5(): String {
	return hashString(this, "MD5")
}

fun String.sha256(): String {
	return hashString(this, "SHA-256")
}

private fun hashString(input: String, algorithm: String): String {
	return MessageDigest
		.getInstance(algorithm)
		.digest(input.toByteArray())
		.fold("") { str, it -> str + "%02x".format(it) }
}