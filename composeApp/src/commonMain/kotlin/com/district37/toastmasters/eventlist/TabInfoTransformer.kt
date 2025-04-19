package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.BackendTabInfo
import com.district37.toastmasters.models.TabInfo
import com.wongislandd.nexus.util.Transformer

class TabInfoTransformer: Transformer<List<BackendTabInfo>, List<TabInfo>> {
    override fun transform(input: List<BackendTabInfo>): List<TabInfo> {
        return input.mapIndexed { index, backendTabInfo ->
            TabInfo(
                displayName = backendTabInfo.displayName,
                dateKey = backendTabInfo.dateKey,
                isSelected = index == 0
            )
        }
    }
}