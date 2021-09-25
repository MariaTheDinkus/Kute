package dev.vini2003.kute.util.extension

private val OrdinalSuffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")

fun Int.ordinal(): String {
	return when (this % 100) {
		11, 12, 13 -> this.toString() + "th"
		else -> "$this${OrdinalSuffixes[this % 10]}"
	}
}