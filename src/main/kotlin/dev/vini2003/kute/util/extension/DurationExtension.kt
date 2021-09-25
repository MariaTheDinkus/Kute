package dev.vini2003.kute.util.extension

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Duration.toHumanReadable(): String {
	if (isInfinite()) {
		return "permanently"
	} else {
		var formatted = ""
		
		val years = inWholeDays - (inWholeDays % 365) / 365
		val months = inWholeDays - (inWholeDays % 30) / 30
		val weeks = inWholeDays - (inWholeDays % 7) / 7
		val days = inWholeDays % 7
		val hours = inWholeHours % 24
		val minutes = inWholeMinutes % 60
		val seconds = inWholeSeconds % 60
		
		if (years >= 1) formatted += "${years}y "
		if (months >= 1) formatted += " ${months}m "
		if (weeks >= 1) formatted += "${weeks}w "
		if (days >= 1) formatted += "${days}d "
		if (hours >= 1) formatted += "${hours}h "
		if (minutes >= 1) formatted += "${minutes}m "
		if (seconds >= 1) formatted += "${seconds}s "
		
		return formatted.trimEnd(' ')
	}
}