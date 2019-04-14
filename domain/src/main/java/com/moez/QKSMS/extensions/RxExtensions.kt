package com.moez.QKSMS.extensions

import io.reactivex.Flowable
import io.reactivex.Observable

data class Optional<out T>(val value: T?) {
    fun notNull() = value != null
}

fun <T, R> Flowable<T>.mapNotNull(mapper: (T) -> R?): Flowable<R> = map { input -> Optional(mapper(input)) }
        .filter { optional -> optional.notNull() }
        .map { optional -> optional.value }

fun <T, R> Observable<T>.mapNotNull(mapper: (T) -> R?): Observable<R> = map { input -> Optional(mapper(input)) }
        .filter { optional -> optional.notNull() }
        .map { optional -> optional.value }

fun <T> Observable<T>.toFlowable(): Flowable<T> = this.toFlowable(io.reactivex.BackpressureStrategy.BUFFER)