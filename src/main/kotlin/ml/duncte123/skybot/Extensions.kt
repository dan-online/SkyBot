package ml.duncte123.skybot

import java.io.Serializable

public data class AtomicPair<A, B>(private var first: A, private var second: B): Serializable {

    constructor(pair: Pair<A, B>) : this(pair.first, pair.second)

    fun getPair(): Pair<A, B> = first to second
    fun setPair(pair: Pair<A, B>) {
        this.first = pair.first
        this.second =  pair.second
    }
    fun setPair(first: A, second: B) {
        this.first = first
        this.second = second
    }

    fun getFirst(): A = first
    fun setFirst(first: A) {
        this.first = first
    }

    fun getSecond(): B = second
    fun setSecond(second: B) {
        this.second = second
    }
}