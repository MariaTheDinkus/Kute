package dev.vini2003.kute.util

fun environmentVariable(name: String): String {
	val variable = System.getenv(name)
	
	if (variable != null) {
		return variable
	} else {
		throw NullPointerException("environment variable $name not present")
	}
}