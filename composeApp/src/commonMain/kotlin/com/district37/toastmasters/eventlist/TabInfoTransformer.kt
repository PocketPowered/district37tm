package com.district37.toastmasters.eventlist

import com.district37.toastmasters.models.BackendTabInfo
import com.district37.toastmasters.models.DateTabInfo
import com.wongislandd.nexus.util.Transformer

class TabInfoTransformer: Transformer<List<BackendTabInfo>, List<DateTabInfo>> {
    override fun transform(input: List<BackendTabInfo>): List<DateTabInfo> {
        return input.mapIndexed { index, backendTabInfo ->
            DateTabInfo(
                displayName = backendTabInfo.displayName,
                dateKey = backendTabInfo.dateKey,
                isSelected = index == 0
            )
        }
    }
}