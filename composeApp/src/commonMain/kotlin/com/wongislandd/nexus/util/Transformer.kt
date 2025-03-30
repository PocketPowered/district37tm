package com.wongislandd.nexus.util

interface Transformer<I, O> {
    fun transform(input: I): O?
}