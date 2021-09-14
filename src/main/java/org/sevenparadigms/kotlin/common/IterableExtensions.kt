package org.sevenparadigms.kotlin.common

fun <T> Iterable<T>.findDiff(b: Iterable<T>): List<T> {
    val itemsInBoth = this.intersect(b)
    return this.subtract(itemsInBoth).toList()
}