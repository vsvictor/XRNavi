package com.mobilespace.xrnavi

internal fun formatProfilePhone(value: String): String {
    val digits = value.filter { it in '0'..'9' }
    if (digits.isEmpty()) return value

    val groups = mutableListOf(digits.take(3))
    var remaining = digits.drop(groups.first().length)
    if (remaining.isNotEmpty()) {
        groups += remaining.take(3)
        remaining = remaining.drop(groups.last().length)
    }
    while (remaining.isNotEmpty()) {
        val groupSize = if (remaining.length > 3) 2 else remaining.length
        groups += remaining.take(groupSize)
        remaining = remaining.drop(groupSize)
    }

    return "+(${groups.first()})" +
        groups.drop(1).takeIf { it.isNotEmpty() }?.joinToString(separator = " ", prefix = " ").orEmpty()
}
