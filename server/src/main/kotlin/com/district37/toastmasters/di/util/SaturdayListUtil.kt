package com.district37.toastmasters.di.util

import com.district37.toastmasters.models.BackendAgendaItem
import com.district37.toastmasters.models.BackendEventDetails

 val saturdayList = listOf(
    //Saturday List
    BackendEventDetails(
        id = 12,
        title = "International Speech Contest Briefings",
        description = "Briefing time",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event2/img2.jpg"
        ),
        time = "Saturday, May 3, 2:30 PM - 3:30 PM",
        locationInfo = "Graham Ballroom / Dilworth Room",
        agenda = listOf(
            BackendAgendaItem(
                title = "ISC Contestant Briefing",
                description = "Contestants, get ready!",
                time = "Saturday, May 3, 2:30 PM - 3:30 PM",
                locationInfo = "Graham Ballroom"
            ),
            BackendAgendaItem(
                title = "ISC Judge's Briefing",
                description = "Judges are briefed",
                time = "Saturday, May 3, 2:30 PM - 3:30 PM",
                locationInfo = "Dilworth Room"
            )
        ),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 13,
        title = "International Speech Contest",
        description = "Watch the amazing speakers compete in ISC",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event2/img2.jpg"
        ),
        time = "Saturday, May 3, 4:00 PM - 5:30 PM",
        locationInfo = "Graham Ballroom",
        agenda = listOf(),
        additionalLinks = listOf()
    )
)