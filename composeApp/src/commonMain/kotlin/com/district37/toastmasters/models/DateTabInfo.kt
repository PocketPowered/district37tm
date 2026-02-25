package com.district37.toastmasters.models

data class DateTabInfo(
    val displayName: String,
    val dateKey: Long,
    val isSelected: Boolean
)

fun List<DateTabInfo>.findSelectedTab(defaultSelectedTab: DateTabInfo? = null): DateTabInfo {
    return this.findSelectedTabOrNull(defaultSelectedTab)
    ?: throw IllegalStateException("No selected tab!")
}

fun List<DateTabInfo>.findSelectedTabOrNull(defaultSelectedTab: DateTabInfo? = null): DateTabInfo? {
    return this.find { it.isSelected } ?: defaultSelectedTab ?: this.firstOrNull()
}
