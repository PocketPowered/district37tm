package com.wongislandd.nexus.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.conditionallyChain(condition: Boolean, modifier: Modifier): Modifier {
    return if (condition) this.then(modifier) else this
}

fun <T> MutableList<T>.addUniqueWithLimit(element: T, maxSize: Int): MutableList<T> {
    this.remove(element)
    this.add(element)
    if (this.size > maxSize) {
        this.removeAt(0) // Removes the first element (oldest)
    }
    return this
}

// add with limit
fun <T> MutableList<T>.addWithLimit(element: T, maxSize: Int): MutableList<T> {
    this.add(element)
    if (this.size > maxSize) {
        this.removeAt(0) // Removes the first element (oldest)
    }
    return this
}

fun Modifier.noIndicationClickable(onClick: () -> Unit = {}) : Modifier {
    return this.clickable(indication = null, interactionSource = null, onClick = onClick)
}

@Composable
fun Float.toDp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}