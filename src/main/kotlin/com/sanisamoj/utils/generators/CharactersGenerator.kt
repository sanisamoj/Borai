package com.sanisamoj.utils.generators

import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.random.Random
import kotlin.text.random

object CharactersGenerator {
    // Generates a character set, with characters accepted as names
    fun generateWithNoSymbols(maxChat: Int = 5): String {

        // Allowed characters
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789@$"

        // Will generate a set of characters
        val characters = (1..maxChat).map { chars.random() }.joinToString("")

        return characters

    }

    fun codeValidationGenerate(): Int {
        return Random.Default.nextInt(100_000, 1_000_000)
    }
}