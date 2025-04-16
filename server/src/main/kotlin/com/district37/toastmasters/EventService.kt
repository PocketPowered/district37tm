package com.district37.toastmasters

import com.district37.toastmasters.models.BackendAgendaItem
import com.district37.toastmasters.models.BackendEventDetails
import com.district37.toastmasters.models.BackendEventPreview
import com.district37.toastmasters.models.BackendExternalLink
import io.ktor.server.plugins.NotFoundException
import kotlinx.datetime.Clock

class EventService {

    fun getEvent(id: Int): BackendEventDetails {
        return MockEventDataProvider.events[id] ?: throw NotFoundException("Event not found")
    }

    fun getEventPreviews(): List<BackendEventPreview> {
        return MockEventDataProvider.eventPreviews
    }
}

object MockEventDataProvider {

    private val eventsSource = listOf(
        BackendEventDetails(
            id = 1,
            title = "First Timers",
            description = "If you are attending for the first time, this is for you",
            images = listOf(
                "\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgHBgkIBwgKCgkLDRYPDQwMDRsUFRAWIB0iIiAdHx8kKDQsJCYxJx8fLT0tMTU3Ojo6Iys/RD84QzQ5OjcBCgoKDQwNGg8PGjclHyU3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3N//AABEIAJQBAwMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAADAAECBAUGB//EADcQAAEDAwMCBQIDBwQDAAAAAAEAAgMEESEFEjETQQYiMlFhFHEjQpEHFSQzgaGxNFLB8GKS0f/EABoBAAMBAQEBAAAAAAAAAAAAAAECAwAEBQb/xAAlEQACAgEEAQUBAQEAAAAAAAAAAQIDEQQSITFBBRMiMlEUcSP/2gAMAwEAAhEDEQA/APNGuRGOCqMd8ojXLmcTqUi+xzfZWoizuAstr/lHjk+VGVeSsZo2IwxWWbLLIZMPdHZUC4aCbkrmlTksrF+F2YsAwVVL3F22O9109H4RqKmkFRPUMhLhdrS0n9fZdz+zvw5TUWm/VVUcctW6Rzdx8waAbC3/AHujGht7ULK5RWTyKJkt7PuLcrZoqPe3dZxHJtlewa74c0/WIrSxMZICCJWCxC0KHT6SgpxBTwMYwC2GjP3QlorZy25wBa2MVnB4nNp8RzYZVd+kNd6Wrtf2kUlJQ1NJPSsbHJNuD2NxcC2f7rM0yPrMBIXl3Ss08nFs7YSjZHODkp9HcwElt1mS0JBttXpdXSNEd7dlydXGBUfCpp9ZOXDFnXEwotIfLwLKFTpMkQ4JXX0fTAyAoVscbwbBVWukp4F9mODhhSuvayc0Z9l0f0gBy1M+Fo7Lp/s/CPsJHNupD7KH0xHZb8kbB7IDmNviytG9sSVSRjdApxD8LpaDRXVpB3hrPdPqugS0MXUa7e0fCX+yG7bnk3sSxk5ro/CXR+FciLX9rIjY7lW9wTYZxhso9H4WwYG7b2yoMgBNkysA4GUIfhMY7dlrmABVqmMDATKeRXDCM8tUCjPFkByqSHUfdK6LTxGaTY0ZKPRkslQuFzlJHloZGyOG08pI74foNkvwrg2Ug4pAJbUQZHDynMh91HaovwEMB3BOu7/cUSKqkjkZI032OBz3sqBflN1Mo7Ewe40e0UPjDSptNZO6uiYQ3zxPdZwPtZVvDf7SWadqFSySEy6fI7c0N5B7kLySNwJHxwr8ROy91Kdf5wPGeeGev+JP2qQSQCLRI3tcSCZJcAfACLF+16kbRgy0DjUW7PG0leIzykE5VczON+coRpl3u5M5x6wd5q3iqo8RauKipe2w8rGN4YPYLs9DO6Fp+F45pMh+rbnuvYPDhvTt+y8b1WpQSwd+knuRq1g/CP2XFV+KghdxWD8ErhtUIbUuubLz9IvmdM3wGpiACfhUtQ1IQeyLFOzpHK5TxFKXSENJsu/TadWWYkRtt2Ryi9NrgPBVOXWSeCsCGOeZwbDG+R2cMaT/AIWkzwzrzm7m6PVllibmPsOcL2Y6OqPg86WpmyUuqvdwgHUn9lXq9NrqK/1lHUQgc743AD+tlVVlTBEnbJnVaH4lbSzRickMByum1LxFSVNNtgeH3GTZeXjBVhkzh5dxXHd6dVOan5OmrWTitrNukded9uLmy042XcPhYulHOVuROyku+LwVreUEe3yFAiNn5R5X2YqRceohDoM+yxKQAVRlBcbqw8uIOENzbNuf0VY8E5GfMLKs5Wqg3KrFdKfBzshZaWgFrdRYX8WKzgjU7unIHg5CSxboOI1bxJM6uWKEyOOOUlzT9Tl3nPdJcS0dn6dv9VZmXS3ID3JNfderg8zJY3ADKHI8WwoSO4QjcrJGyRcblJMnTiBYuVpU4/CKzIvUtWlF4SpWcFYGfVetV1Zq/WoQQOlIANkU8RyxdrlLCDaX/q2/dex+GP8ATN+y8roNOdHUBzjhepeGZA2AA+y8P1WcZJYPU0tM618kbVe4NgN151r84bOSPddjr1Q+Knc4HsvLdXrJHzvv3XN6bTvnuKXS2wCNrZ5Jm09Ox75JDZjG5Lj7LtNG8CwmKOr10tqH7wyanbJt6F+MfmP3x/zl/sw00z1c+oyNdvY1zIXgX2nygu5Hd7f/AFPuvQKqpMzz5Ws6zem4HqBrnNaSMhtzcB17DDb3N3WX0UKox6R5FljkAhpKWgY6HTKSKN0LtzWxsFpoz+Wwyf8AG0F3YXqvnuOnT3bva6aCV0ob0w0EybwWnyYIbbdc2xbKaeYlsUhEpLHbDAHbXyNcLtYGPb638EX8rRyLqvK2SCJwa6B76eZrmBkLyHyXs1mw7nMYw7SXNuDkd7p8Ew5qH9J7nMjNKWCcRfUxuIjPl2uvdpO43FjtsPV2XJ+IfBUFUySo0pj6esBI+mczayYgX8nmLRjIse/A7dI+rjc5pgkpagMn3U7jSlzX7sSPuPyAHbuGRe5Fgowy07tsUE8RhbY3heY3dNuS7a7yuDhcWb8ZHY9APGXscx5Y8FrmEtLT2I5Tt5C7rx7o43t1OMMa/cIp2sBbnJbZtsY/oey5FtNttdZySHjFst6WbHK2Y3tvlYcJ6aK6rt3XHZW5yyddc1Fcm6XM2IDDFdY5rj7lDFYUI0tBdyN58sQwgTvB9PCw5Kp5dyr0Eu6PPKf2tonuZGlyVWcjSuF8Ku96qlwSbF3UgUMPypXujgIKQ+d33SUX+o/dOnwIVZRlMzlEmQ2chOIScCmJFkWUYFlXJQCRTpkgiBhYvUtej/lFY8eHrZosxlSt6KV9mfWD8RaWjUYf5nLPrMSLf8NR/UMt3UNTJxq4OvRbff5DVjmwMFhwtjwxqoc9rb2WJ4goZos2dZY2kVrqWrbckC64Xp1bS35O/Uape5t8Hr+qsbPR7rg3C8s16Lp1DrC9zYL0CkrRUUAN8Lm5aSD9+0T64ENlkvEzs6x5d8f/AA/C5vTU42uLI6lr28nTeDiKOifS2Me2ls8OINj1GbvzNA9bTz+qtvnJEjmZDPzGKEtYR5mlx33IBv5W2JNhcgFc34Z1EwV95H7A9xc8sBjAZIAx5HNrO6JxxyugqjKK6dj+sXwSBwaGCSxvkt6tjfa8i7CbC+Lr6LB4r5YWTqt60cTpmtN7iCJz3W3C7enJ5nSODsuzsHugVFS2KlHWJhu36ZjoXNdbDh9PTvNi2Q2G4uBb/hVDUxtp2tmYLW6Ef8eZATtsYqeXDo3eU7i7J93cIklXMyeofNVxxtYWwyTNB20rHFtoS3fmU7sSZt8cjAA1Fc+73VFbUUzhaGoEcIAjDuKcDf8AznbvWMYyLXU/rDMQJKsOk3NJhqYDZr7jZHgG2293ntbcOMNLIYHOa2sng+lYyIdUF4p7lnkkz553duw/QiUUkj5OizUGO2FsXSnju5t2k9NxcCDI4XDzwGnOEXyAjqcP1WhVNOQy3RIjkgdeNzgbi977BuyR7DBHC8sdU3ALTgi4XqOuFtNptXK6OJr3x3L4v5Ul7AeUn08C2bgAYuSPJZMG3ZLtTKRbSCmoKiZighJHCDlhDKVKN5JQVONZgDOvcK5A/wDDVR3AR6Y+VIxkwjnEqu9yskYVaTlFGZFpyrDOLqs3lWW5CEkGIN9tx+6Sg4+Y/dJMYrvduUL2SN1EpyZYZd2FN1M+/ChR5kF10TYWdAG2bKFlmxnRVXvRzcke1B7q9XEB5sqRVYvKyRmsPBJh8wW1QfyysRnqC29Oyyyld9R6+ylXD8Rb/g+rihNpMWPKyamDfIp00Jjy0kKVqU4YZapNW8HWeJtRpnwgRm5suCLv4gP4AytOfe8WJLvhXTTU/hpkdXqMUc+qEbqehflsPs+X59m/qhQlBYXJXUwe5NmmzVWaHQsdXxEzviD4Kc43k8F3s3v7nCpeH6mXU/31V1QZNXsijmjc4ZDWuu4N9hhg7YXOV089Y0VtTI+WWWR/Ukc7JdZp/pg/2Vnw5qX7r1VlS/MZaWPAuCWnByLEf07q1OnjU2/LOO65zWDoapzGVL6mBvVjdumb+G0dWJ4Jc0HknzWt7Ae2OlFfDqenmSpc2Srp4hG/qwul6zLENkuMxcNDnNtYh1/ylcVrEZ0mojY0udSZfSSNZw0ncWAngg2IN8Z5yosr3UT21DJt0D5JA5zm7+m53qa6/le17T5m4vza4zZ8ElyjsKuQs67ZJHzsdE0TOMADy29mxyDh0R3W6g8wNjc5UJpwGzlpdFM1sdLCWgu2E7CaeMXG/cASJHXt8cjPiqP3o0QUs0nWkHlge8P8t/OwY88JGAwd7X4uAfvJnXeJZalgc78W7yHua3MTSchgabtIaL2wTlAODoWP85jhq6qNhnZTx23SGJoy5rL33TBwF3nDb4IyRZpw97TK40To3McS3YC1gcci4Dd7PKScfiONj5bhYMWsUsccZnq2gGMtJdCGtIf/ADANoFm3sdjSL2O4kXBy9f8AE8k46dK6nu43e67g5x4t5XAWsBi1rCwsL3IrRf8AGVfC2kdTwBnUlcQS197NzcCxtn7e9uVwEpu4q46Z8znSSHzHPJNvtckqnLylT5KYwiASSCSYAlKM5UFOPlZgDvOAiwGwQX8BFiSjFi+FWkVgcKvIggsGD5lZjPlKrDlGbwszRBu9RSSdyUkTApo9jiEHutDU2tEz9izytF5Q10VGTSD0hs8fddA6QimGey52A2eFtvd/Df0Uro5aKUSwmY1S68jkBEm9SGFddHO+WOw+YLd0wXCw2DzBb2ljCjd9StS5Czja1xCDC8lwAP2HcomozNgZ5u/A7lYbpXPlDyeDcfCSuG6JZ3KqW5dnUVFYzQmbogybVXC8ZFnNpv8Ay9i/27BctNNJPK6aZ7pJHkuc95uXH3TyYP3QySeSrQrjDojdfO2WZB6V7XMlp5DYSWc0/wC14vY/oSD979kA48pBBCY4SJJyTdUIh5K2olpW0j5nGFhu1p/L9jyhwzyROBY5wc03BBtn3QwL88J7gcIBDRTSRYY8bTkMJxf3HcH5GVpN16ZzQ2upaauYRYmpZ+Lb4kFndrXNyM5yVlU7epJtOVqTUTW0+4cgJJTUSsKnNZQKo1CjkaelQyRF3YVRcOb92rO3mxAuB2CTxZxBUU6JNYLcPoVeb1FWIeECb1FKuxn0DCSSZOIJTZyoKTOUGYO70hSicoO9IUo+UBi0HYQHog4QnoBIDlGbwgDlHj4K0jRIO9RSSPKSxgU83VeSgFOCnI9kyWBW2+yUPqC1Xn+GWVD6wtKc2gCnPtFYfVmZIblQHKk7lJgu66p4JeSxTsuQtL6ltHBf1OPDflUGkDaG8/CfUd0cohkBD2AXB9yptbnyUztRXmlfLJ1JCS//AAnkc3cSwWFhhQbjnKiSqLjhEnzyyzK6OWKN0Y2yNbaRvvbgj/kf1VZMHEG4JB+FNr2PeOqwgdy3lEAP7qbBc27IlLTGoftBwPVfgI08TIAWs5HulclnBRQeMsqkqKSSYQPQ/wA9q3ao/wAIb+yxNOH462q8gUhv7Lju+6PR0y/4yZzz+T91FSf6iorr8HnPstQHCDP6kSEoc3qSrsZ9AkkgknFEnbymTtOVjFg+gJR8pH0BKP1JEMWBwhPRRwhPWMDHKNHwgjlHi4K0gxIu9RSSIyUlglXbt5TjhEqC3sgtNkwhKO7XK3PIDGAqZOVJz7hBoKeAZU4mPkdtZa5xlDVukd0huA810fAqZq1IpKSOKGjid1g0GSaQ3N7cD4Wdq05qawzfmc1t/uBZElk3ne4qrOd2LdvLbupxzEq1kAmRJIXxi7ghqieSbWBWTgZTsG5wFwL++E8jdri0EG3cd0RWHin6MW1vPugPe5xJJUC5IJcLOSm54wJJSskRwmFwWtNb+NdaGovtT2PdQ0endfcQm1o7SGrleJWnfHMKP9Mh/KinKZdJwMsQlQm5Tw8ppeUF2HwCCSSSYUSQSSasYsfkCdnKYHypM9SRDBxwoPUxwoPRMCHKPFwgd0aM4CEgxHPJSTO5KdDASiXE8pk9krKhMYcosTDI9rG8uNlDaluLcoMzzjg0tT0l+nMhkdKyRsgxt7H2KosftyVbdXdah+mqg7c07mPBH91RblyEc+SNG/bifZN0hcccFaGlxN6m4i5+VSbGXG61dNj2nKnbL4nZWnuB6qM3CyRzYrZ1dYvBuEafqC5fIIIyWAlmDwRlQW94c1HTNJqxPqNE6vikp3MdATt6b74IPcd1i1Lmvmkexu0OcSG+wVvBDPIBSBUU7eUo6JpwPMFLbhRJsfssE3aGqZFHyOFl6jN1piVXEhta6gSpQqUZbi9moc4KJNjNykYD7KdICXD3WqKcFvCE57WLCG4yWxkZQpeVpyxbAcLNl5KaEtws4bQSZOmVSQkm8pJwgYM3hJpymb6UgcoDB2nCi5RD8JFyxske6IzhCupNdZZmQQ8pIZeksHIEcqdhZJJMKIBRcEkkDMY8KUfqSSQfRvJdhAxhaFOSALJJLns6Ousr6kSRlZB5SSVKeiFv2GJJ5JTt9CSSuRIqTOUkkoyDILuUkljMYKTRdJJYxo0MbQWlbAGAkkuDUdnfR9SnWd1izeopJK9HRG/sEmSSXScok6ZJYwVvpTd0kkoWSSSSRMMVElMkiAV0ySSwT//Z\"",
                "https://example.com/event1/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds,
            locationInfo = "Charlotte NC",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Opening Ceremony",
                    description = "Welcome speech and introduction to the event.",
                    time = Clock.System.now().epochSeconds + 3600,
                    locationInfo = "Main Hall"
                ),
                BackendAgendaItem(
                    title = "Keynote Speech",
                    description = "A talk on the future of technology.",
                    time = Clock.System.now().epochSeconds + 7200,
                    locationInfo = "Conference Room A"
                )
            ),
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
            title = "International Speech Contest",
            description = "Watch the amazing speakers compete for ISC",
            images = listOf(
                "https://d100tm.org/wp-content/uploads/2023/10/100-years-sparkle-district-100.png",
                "https://example.com/event2/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 86400,
            locationInfo = "Los Angeles, CA",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Networking Session",
                    description = "An opportunity to connect with industry professionals.",
                    time = Clock.System.now().epochSeconds + 90000,
                    locationInfo = "Lobby Area"
                ),
                BackendAgendaItem(
                    title = "Panel Discussion",
                    description = "Experts discuss trends in AI and Machine Learning.",
                    time = Clock.System.now().epochSeconds + 93600,
                    locationInfo = "Main Auditorium"
                )
            ),
            additionalLinks = listOf(
                BackendExternalLink(
                    displayName = "Speaker List",
                    url = "https://example.com/event2/speakers"
                ),
                BackendExternalLink(
                    displayName = "Live Stream",
                    url = "https://example.com/event2/live"
                )
            )
        ),
        BackendEventDetails(
            id = 3,
            title = "District 37 Evaluation contest",
            description = "Join us in San Francisco for workshops, panels, and hands-on experiences in tech.",
            images = listOf(
                "https://d100tm.org/wp-content/uploads/2023/10/100-years-sparkle-district-100.png",
                "https://example.com/event3/img2.jpg"
            ),
            time = Clock.System.now().epochSeconds + 172800,
            locationInfo = "San Francisco, CA",
            agenda = listOf(
                BackendAgendaItem(
                    title = "Workshop: Kotlin for Android",
                    description = "A hands-on coding session on modern Android development.",
                    time = Clock.System.now().epochSeconds + 175200,
                    locationInfo = "Tech Hub Room B"
                ),
                BackendAgendaItem(
                    title = "Closing Remarks",
                    description = "A summary of the event and future initiatives.",
                    time = Clock.System.now().epochSeconds + 180000,
                    locationInfo = "Grand Hall"
                )
            ),
            additionalLinks = listOf(
                BackendExternalLink(
                    displayName = "Kotlin Docs",
                    url = "https://kotlinlang.org/docs/"
                ),
                BackendExternalLink(
                    displayName = "Event Recap",
                    url = "https://example.com/event3/recap"
                )
            )
        )
    )

    val events = eventsSource.associateBy { it.id }

    val eventPreviews = eventsSource.map {
        BackendEventPreview(
            id = it.id,
            title = it.title,
            image = it.images.firstOrNull() ?: "no url in mock",
            time = it.time,
            locationInfo = it.locationInfo
        )
    }
}