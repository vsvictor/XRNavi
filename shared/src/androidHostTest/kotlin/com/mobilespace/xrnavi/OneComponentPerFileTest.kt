package com.mobilespace.xrnavi

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OneComponentPerFileTest {
    private val sourceRoot = File("src/commonMain/kotlin")
    private val component = Regex("""^@Composable\s*\n(?:@\w+[^\n]*\n)*(?:(?:private|internal|public)\s+)?fun\s+(?:\w+\.)?([A-Z]\w*)\s*\(""", RegexOption.MULTILINE)

    @Test
    fun everyComponentLivesInItsOwnFileNamedAfterIt() {
        val violations = sourceRoot.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            val names = component.findAll(file.readText()).map { it.groupValues[1] }.toList()
            when {
                names.size > 1 -> listOf("${file.name}: ${names.joinToString()}")
                names.size == 1 && names.single() != file.nameWithoutExtension -> listOf("${file.name}: ${names.single()}")
                else -> emptyList()
            }
        }.toList()
        assertTrue(violations.isEmpty(), "Один компонент на файл: $violations")
    }
}
