package com.mobilespace.xrnavi

private val profileDatePattern = Regex("""^(\d{4})-(\d{2})-(\d{2})(?:$|[Tt ].*)$""")

internal fun formatAuthenticatedProfileDate(value: String): String {
    val match = profileDatePattern.matchEntire(value) ?: return "Не вказано"
    val year = match.groupValues[1].toIntOrNull() ?: return "Не вказано"
    val month = match.groupValues[2].toIntOrNull() ?: return "Не вказано"
    val day = match.groupValues[3].toIntOrNull() ?: return "Не вказано"
    val daysInMonth = when (month) {
        2 -> if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) 29 else 28
        4, 6, 9, 11 -> 30
        in 1..12 -> 31
        else -> return "Не вказано"
    }
    if (year == 0 || day !in 1..daysInMonth) return "Не вказано"
    return "${match.groupValues[3]}.${match.groupValues[2]}.${match.groupValues[1]}"
}
