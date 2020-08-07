package xyz.jpenilla.announcerplus.util

import java.util.*
import kotlin.random.Random

class RandomCollection<E> {
    private val map = TreeMap<Double, E>()
    private var total = 0.0

    fun add(weight: Double, result: E): RandomCollection<E> {
        if (weight <= 0) return this
        total += weight
        map[total] = result
        return this
    }

    fun next(): E {
        val value = Random.nextDouble() * total
        return map.higherEntry(value).value
    }
}