package com.district37.toastmasters.models

data class TabInfo(
    val displayName: String,
    val dateKey: String,
    val isSelected: Boolean
)

fun List<TabInfo>.findSelectedTab(defaultSelectedTab: TabInfo? = null): TabInfo {
    return this.find { it.isSelected } ?: defaultSelectedTab
    ?: throw IllegalStateException("No selected tab!")
}