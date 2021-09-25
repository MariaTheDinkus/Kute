package dev.vini2003.kute.util

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

fun <T : Comparable<T>, E : Entity<T>> EntityClass<T, E>.all(limit: Int = Int.MAX_VALUE): List<E> {
	return transaction {
		this@all.all().limit(limit).toList()
	}
}

fun <T : Comparable<T>, E : Entity<T>> EntityClass<T, E>.find(column: Column<EntityID<T>>, value: T, limit: Int = Int.MAX_VALUE): List<E> {
	return transaction {
		this@find.find {
			column eq value
		}.limit(limit).toList()
	}
}

fun <T : Comparable<T>, V, E : Entity<T>> EntityClass<T, E>.find(column: Column<V>, value: V, limit: Int = Int.MAX_VALUE): List<E> {
	return transaction {
		this@find.find {
			column eq value
		}.limit(limit).toList()
	}
}

fun <T : Comparable<T>, V, E : Entity<T>> EntityClass<T, E>.firstOrNull(column: Column<V>, value: V): E? {
	return if (contains(column, value)) {
		return transaction {
			this@firstOrNull.find {
				column eq value
			}.first()
		}
	} else {
		null
	}
}

fun <T : Comparable<T>, E : Entity<T>> EntityClass<T, E>.firstOrNull(column: Column<EntityID<T>>, value: T): E? {
	return if (contains(column, value)) {
		return transaction {
			this@firstOrNull.find {
				column eq value
			}.first()
		}
	} else {
		null
	}
}

fun <T : Comparable<T>, V, E : Entity<T>> EntityClass<T, E>.first(column: Column<V>, value: V): E {
	return transaction {
		this@first.find {
			column eq value
		}.first()
	}
}

fun <T : Comparable<T>, E : Entity<T>> EntityClass<T, E>.first(column: Column<EntityID<T>>, value: T): E {
	return transaction {
		this@first.find {
			column eq value
		}.first()
	}
}

fun <T : Comparable<T>, E : Entity<T>> EntityClass<T, E>.contains(column: Column<EntityID<T>>, value: T): Boolean {
	return transaction {
		count(Op.build {
			column eq value
		}) > 0
	}
}

fun <T : Comparable<T>, V, E : Entity<T>> EntityClass<T, E>.contains(column: Column<V>, value: V): Boolean {
	return transaction {
		count(Op.build {
			column eq value
		}) > 0
	}
}

fun <T : Entity<ID>, ID : Comparable<ID>> EntityClass<ID, T>.create(id: ID?, block: T.() -> Unit) {
	transaction {
		this@create.new(id, block)
	}
}

fun <T : Entity<ID>, ID : Comparable<ID>> EntityClass<ID, T>.create(block: T.() -> Unit) {
	transaction {
		this@create.new(block)
	}
}

fun <T : Comparable<T>> IdTable<T>.delete(column: Column<EntityID<T>>, value: T) {
	transaction {
		this@delete.deleteWhere {
			column eq value
		}
	}
}

fun <V> Table.delete(column: Column<V>, value: V) {
	transaction {
		this@delete.deleteWhere {
			column eq value
		}
	}
}