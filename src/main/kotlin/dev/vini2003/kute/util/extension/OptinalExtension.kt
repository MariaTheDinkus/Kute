package dev.vini2003.kute.util.extension

import java.util.*

val <T> Optional<T>.orNull: T?
	get() {
		return if (isPresent) {
			get()
		} else {
			null
		}
	}