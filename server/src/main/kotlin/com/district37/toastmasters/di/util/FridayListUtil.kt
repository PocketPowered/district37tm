package com.district37.toastmasters.di.util

import com.district37.toastmasters.models.BackendAgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendExternalLink

val fridayList = listOf(
    //Friday List
    BackendEventDetails(
        id = 1,
        title = "Registration / First Timers Check-In",
        description = "If you are attending for the first time, this is for you",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 2:00 PM - 5:45 PM",
        isDayOne = true,
        locationInfo = "Near Queens Ballroom",
        agenda = listOf(),
        additionalLinks = listOf(
            BackendExternalLink(
                displayName = "Event Website",
                url = "https://example.com/event1"
            ),
            BackendExternalLink(
                displayName = "Register Here",
                url = "https://example.com/event1/register"
            )
        )
    ),
    BackendEventDetails(
        id = 2,
        title = "D37 Store / Silent Auction Treasures",
        description = "Buy stuff here",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 2:00 PM - 5:45 PM \nFriday, May 2, 8:30 PM - 10:00 PM",
        locationInfo = "Noda Room",
        agenda = listOf(),
        additionalLinks = listOf(
            BackendExternalLink(
                displayName = "Event Website",
                url = "https://example.com/event1"
            ),
            BackendExternalLink(
                displayName = "Register Here",
                url = "https://example.com/event1/register"
            )
        )
    ),
    BackendEventDetails(
        id = 3,
        title = "D37 Author's Showcase",
        description = "Books, Books and More Books",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 2:00 PM - 6:00 PM",
        locationInfo = "Midwood Room",
        agenda = listOf(),
        additionalLinks = listOf(
            BackendExternalLink(
                displayName = "Event Website",
                url = "https://example.com/event1"
            ),
            BackendExternalLink(
                displayName = "Register Here",
                url = "https://example.com/event1/register"
            )
        )
    ),
    BackendEventDetails(
        id = 4,
        title = "50/50 Raffle Purchases",
        description = "Buy for the Raffle here",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 2:00 PM - 5:30 PM \nFriday, May 2, 7:00 PM - 11:00 PM",
        locationInfo = "Reach out to Julie Richardt & Valencia Tims, who are in colorful hats",
        agenda = listOf(),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 5,
        title = "Club Pride Baskets",
        description = "Cheer for your Clubs",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 2:00 PM - 3:30 PM \nFriday, May 2, 7:30 PM - 11:00 PM",
        locationInfo = "Graham Ballroom",
        agenda = listOf(),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 6,
        title = "Evaluation contest Briefings",
        description = "Evaluation Contest functionaries",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event3/img2.jpg"
        ),
        time = "Friday, May 2, 4:00 PM - 5:00 PM",
        locationInfo = "Graham Ballroom and Dilworth Room",
        agenda = listOf(
            BackendAgendaItem(
                title = "Evaluation Contest Contestant Briefing",
                description = "Briefing details",
                time = "Friday, May 2, 4:00 PM - 5:00 PM",
                locationInfo = "Graham Ballroom"
            ),
            BackendAgendaItem(
                title = "Evaluation Contest Judge's Briefing",
                description = "Judges assemble!",
                time = "Friday, May 2, 4:00 PM - 5:00 PM",
                locationInfo = "Dilworth Room"
            )
        ),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 7,
        title = "Opening Ceremony / Welcome",
        description = "Let's have some fun",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 5:30 PM - 6:00 PM",
        locationInfo = "Graham Ballroom",
        agenda = listOf(),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 8,
        title = "Evaluation Speech Contest",
        description = "Contest Time",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 6:00 PM - 7:00 PM",
        locationInfo = "Graham Ballroom",
        agenda = listOf(),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 9,
        title = "Registration Desk & First Timers Check IN",
        description = "Register!",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 7:00 PM - 7:30 PM",
        locationInfo = "Near Queens Ballroom",
        agenda = listOf(),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 10,
        title = "Dinner / Registration",
        description = "look for text message",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 7:30 PM - 9:00 PM",
        locationInfo = "Queens Ballroom / Text Message",
        agenda = listOf(
            BackendAgendaItem(
                title = "Buffet Dinner",
                description = "Let's Eat",
                time = "Friday, May 2, 7:30 PM - 9:00 PM",
                locationInfo = "Queens Ballroom"
            ),
            BackendAgendaItem(
                title = "Registration & First Timers",
                description = "Check your phones",
                time = "Friday, May 2, 7:30 PM - 9:00 PM",
                locationInfo = "Contact via TEXT MESSAGE"
            )
        ),
        additionalLinks = listOf()
    ),
    BackendEventDetails(
        id = 11,
        title = "Toastpardy",
        description = "Clash of the Toastmasters & Toasted Tales",
        images = listOf(
            "https://d37toastmasters.org/wp-content/uploads/2025/04/100-years-sparkle-district-37.png",
            "https://example.com/event1/img2.jpg"
        ),
        time = "Friday, May 2, 9:00 PM - 11:30 PM",
        locationInfo = "Hospitality Suite Room",
        agenda = listOf(),
        additionalLinks = listOf()
    )
)