package com.sanisamoj.data.models.enums

enum class StorageUnit(val bytes: Long) {
    BYTES(1),
    KILOBYTES(1024),
    MEGABYTES(1024 * 1024),
    GIGABYTES(1024 * 1024 * 1024);

    fun toBytes(value: Double): Long = (value * bytes).toLong()

    fun fromBytes(bytes: Long): Double = bytes.toDouble() / this.bytes
}