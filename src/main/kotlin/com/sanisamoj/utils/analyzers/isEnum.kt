package com.sanisamoj.utils.analyzers

inline fun <reified T : Enum<T>> String.isInEnum(): Boolean {
    return enumValues<T>().any { it.name == this }
}
