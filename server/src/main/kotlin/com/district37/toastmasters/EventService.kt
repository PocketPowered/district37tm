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
                "https://www.google.com/imgres?q=toastmasters%20100%20years%20district%2037&imgurl=https%3A%2F%2Fd100tm.org%2Fwp-content%2Fuploads%2F2023%2F10%2F100-years-sparkle-district-100.png&imgrefurl=https%3A%2F%2Fd100tm.org%2F&docid=BRKeUkUFFy3IaM&tbnid=BZ8kI1JF0dmjIM&vet=12ahUKEwjl05f6x9uMAxXdGtAFHexCIhwQM3oECBUQAA..i&w=1200&h=1287&hcb=2&ved=2ahUKEwjl05f6x9uMAxXdGtAFHexCIhwQM3oECBUQAA",
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
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgHBgkIBwgKCgkLDRYPDQwMDRsUFRAWIB0iIiAdHx8kKDQsJCYxJx8fLT0tMTU3Ojo6Iys/RD84QzQ5OjcBCgoKDQwNGg8PGjclHyU3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3N//AABEIAJQBAwMBIgACEQEDEQH/xAAbAAABBQEBAAAAAAAAAAAAAAADAAECBAUGB//EADcQAAEDAwMCBQIDBwQDAAAAAAEAAgMEESEFEjETQQYiMlFhFHEjQpEHFSQzgaGxNFLB8GKS0f/EABoBAAMBAQEBAAAAAAAAAAAAAAECAwAEBQb/xAAlEQACAgEEAQUBAQEAAAAAAAAAAQIDEQQSITFBBRMiMlEUcSP/2gAMAwEAAhEDEQA/APNGuRGOCqMd8ojXLmcTqUi+xzfZWoizuAstr/lHjk+VGVeSsZo2IwxWWbLLIZMPdHZUC4aCbkrmlTksrF+F2YsAwVVL3F22O9109H4RqKmkFRPUMhLhdrS0n9fZdz+zvw5TUWm/VVUcctW6Rzdx8waAbC3/AHujGht7ULK5RWTyKJkt7PuLcrZoqPe3dZxHJtlewa74c0/WIrSxMZICCJWCxC0KHT6SgpxBTwMYwC2GjP3QlorZy25wBa2MVnB4nNp8RzYZVd+kNd6Wrtf2kUlJQ1NJPSsbHJNuD2NxcC2f7rM0yPrMBIXl3Ss08nFs7YSjZHODkp9HcwElt1mS0JBttXpdXSNEd7dlydXGBUfCpp9ZOXDFnXEwotIfLwLKFTpMkQ4JXX0fTAyAoVscbwbBVWukp4F9mODhhSuvayc0Z9l0f0gBy1M+Fo7Lp/s/CPsJHNupD7KH0xHZb8kbB7IDmNviytG9sSVSRjdApxD8LpaDRXVpB3hrPdPqugS0MXUa7e0fCX+yG7bnk3sSxk5ro/CXR+FciLX9rIjY7lW9wTYZxhso9H4WwYG7b2yoMgBNkysA4GUIfhMY7dlrmABVqmMDATKeRXDCM8tUCjPFkByqSHUfdK6LTxGaTY0ZKPRkslQuFzlJHloZGyOG08pI74foNkvwrg2Ug4pAJbUQZHDynMh91HaovwEMB3BOu7/cUSKqkjkZI032OBz3sqBflN1Mo7Ewe40e0UPjDSptNZO6uiYQ3zxPdZwPtZVvDf7SWadqFSySEy6fI7c0N5B7kLySNwJHxwr8ROy91Kdf5wPGeeGev+JP2qQSQCLRI3tcSCZJcAfACLF+16kbRgy0DjUW7PG0leIzykE5VczON+coRpl3u5M5x6wd5q3iqo8RauKipe2w8rGN4YPYLs9DO6Fp+F45pMh+rbnuvYPDhvTt+y8b1WpQSwd+knuRq1g/CP2XFV+KghdxWD8ErhtUIbUuubLz9IvmdM3wGpiACfhUtQ1IQeyLFOzpHK5TxFKXSENJsu/TadWWYkRtt2Ryi9NrgPBVOXWSeCsCGOeZwbDG+R2cMaT/AIWkzwzrzm7m6PVllibmPsOcL2Y6OqPg86WpmyUuqvdwgHUn9lXq9NrqK/1lHUQgc743AD+tlVVlTBEnbJnVaH4lbSzRickMByum1LxFSVNNtgeH3GTZeXjBVhkzh5dxXHd6dVOan5OmrWTitrNukded9uLmy042XcPhYulHOVuROyku+LwVreUEe3yFAiNn5R5X2YqRceohDoM+yxKQAVRlBcbqw8uIOENzbNuf0VY8E5GfMLKs5Wqg3KrFdKfBzshZaWgFrdRYX8WKzgjU7unIHg5CSxboOI1bxJM6uWKEyOOOUlzT9Tl3nPdJcS0dn6dv9VZmXS3ID3JNfderg8zJY3ADKHI8WwoSO4QjcrJGyRcblJMnTiBYuVpU4/CKzIvUtWlF4SpWcFYGfVetV1Zq/WoQQOlIANkU8RyxdrlLCDaX/q2/dex+GP8ATN+y8roNOdHUBzjhepeGZA2AA+y8P1WcZJYPU0tM618kbVe4NgN151r84bOSPddjr1Q+Knc4HsvLdXrJHzvv3XN6bTvnuKXS2wCNrZ5Jm09Ox75JDZjG5Lj7LtNG8CwmKOr10tqH7wyanbJt6F+MfmP3x/zl/sw00z1c+oyNdvY1zIXgX2nygu5Hd7f/AFPuvQKqpMzz5Ws6zem4HqBrnNaSMhtzcB17DDb3N3WX0UKox6R5FljkAhpKWgY6HTKSKN0LtzWxsFpoz+Wwyf8AG0F3YXqvnuOnT3bva6aCV0ob0w0EybwWnyYIbbdc2xbKaeYlsUhEpLHbDAHbXyNcLtYGPb638EX8rRyLqvK2SCJwa6B76eZrmBkLyHyXs1mw7nMYw7SXNuDkd7p8Ew5qH9J7nMjNKWCcRfUxuIjPl2uvdpO43FjtsPV2XJ+IfBUFUySo0pj6esBI+mczayYgX8nmLRjIse/A7dI+rjc5pgkpagMn3U7jSlzX7sSPuPyAHbuGRe5Fgowy07tsUE8RhbY3heY3dNuS7a7yuDhcWb8ZHY9APGXscx5Y8FrmEtLT2I5Tt5C7rx7o43t1OMMa/cIp2sBbnJbZtsY/oey5FtNttdZySHjFst6WbHK2Y3tvlYcJ6aK6rt3XHZW5yyddc1Fcm6XM2IDDFdY5rj7lDFYUI0tBdyN58sQwgTvB9PCw5Kp5dyr0Eu6PPKf2tonuZGlyVWcjSuF8Ku96qlwSbF3UgUMPypXujgIKQ+d33SUX+o/dOnwIVZRlMzlEmQ2chOIScCmJFkWUYFlXJQCRTpkgiBhYvUtej/lFY8eHrZosxlSt6KV9mfWD8RaWjUYf5nLPrMSLf8NR/UMt3UNTJxq4OvRbff5DVjmwMFhwtjwxqoc9rb2WJ4goZos2dZY2kVrqWrbckC64Xp1bS35O/Uape5t8Hr+qsbPR7rg3C8s16Lp1DrC9zYL0CkrRUUAN8Lm5aSD9+0T64ENlkvEzs6x5d8f/AA/C5vTU42uLI6lr28nTeDiKOifS2Me2ls8OINj1GbvzNA9bTz+qtvnJEjmZDPzGKEtYR5mlx33IBv5W2JNhcgFc34Z1EwV95H7A9xc8sBjAZIAx5HNrO6JxxyugqjKK6dj+sXwSBwaGCSxvkt6tjfa8i7CbC+Lr6LB4r5YWTqt60cTpmtN7iCJz3W3C7enJ5nSODsuzsHugVFS2KlHWJhu36ZjoXNdbDh9PTvNi2Q2G4uBb/hVDUxtp2tmYLW6Ef8eZATtsYqeXDo3eU7i7J93cIklXMyeofNVxxtYWwyTNB20rHFtoS3fmU7sSZt8cjAA1Fc+73VFbUUzhaGoEcIAjDuKcDf8AznbvWMYyLXU/rDMQJKsOk3NJhqYDZr7jZHgG2293ntbcOMNLIYHOa2sng+lYyIdUF4p7lnkkz553duw/QiUUkj5OizUGO2FsXSnju5t2k9NxcCDI4XDzwGnOEXyAjqcP1WhVNOQy3RIjkgdeNzgbi977BuyR7DBHC8sdU3ALTgi4XqOuFtNptXK6OJr3x3L4v5Ul7AeUn08C2bgAYuSPJZMG3ZLtTKRbSCmoKiZighJHCDlhDKVKN5JQVONZgDOvcK5A/wDDVR3AR6Y+VIxkwjnEqu9yskYVaTlFGZFpyrDOLqs3lWW5CEkGIN9tx+6Sg4+Y/dJMYrvduUL2SN1EpyZYZd2FN1M+/ChR5kF10TYWdAG2bKFlmxnRVXvRzcke1B7q9XEB5sqRVYvKyRmsPBJh8wW1QfyysRnqC29Oyyyld9R6+ylXD8Rb/g+rihNpMWPKyamDfIp00Jjy0kKVqU4YZapNW8HWeJtRpnwgRm5suCLv4gP4AytOfe8WJLvhXTTU/hpkdXqMUc+qEbqehflsPs+X59m/qhQlBYXJXUwe5NmmzVWaHQsdXxEzviD4Kc43k8F3s3v7nCpeH6mXU/31V1QZNXsijmjc4ZDWuu4N9hhg7YXOV089Y0VtTI+WWWR/Ukc7JdZp/pg/2Vnw5qX7r1VlS/MZaWPAuCWnByLEf07q1OnjU2/LOO65zWDoapzGVL6mBvVjdumb+G0dWJ4Jc0HknzWt7Ae2OlFfDqenmSpc2Srp4hG/qwul6zLENkuMxcNDnNtYh1/ylcVrEZ0mojY0udSZfSSNZw0ncWAngg2IN8Z5yosr3UT21DJt0D5JA5zm7+m53qa6/le17T5m4vza4zZ8ElyjsKuQs67ZJHzsdE0TOMADy29mxyDh0R3W6g8wNjc5UJpwGzlpdFM1sdLCWgu2E7CaeMXG/cASJHXt8cjPiqP3o0QUs0nWkHlge8P8t/OwY88JGAwd7X4uAfvJnXeJZalgc78W7yHua3MTSchgabtIaL2wTlAODoWP85jhq6qNhnZTx23SGJoy5rL33TBwF3nDb4IyRZpw97TK40To3McS3YC1gcci4Dd7PKScfiONj5bhYMWsUsccZnq2gGMtJdCGtIf/ADANoFm3sdjSL2O4kXBy9f8AE8k46dK6nu43e67g5x4t5XAWsBi1rCwsL3IrRf8AGVfC2kdTwBnUlcQS197NzcCxtn7e9uVwEpu4q46Z8znSSHzHPJNvtckqnLylT5KYwiASSCSYAlKM5UFOPlZgDvOAiwGwQX8BFiSjFi+FWkVgcKvIggsGD5lZjPlKrDlGbwszRBu9RSSdyUkTApo9jiEHutDU2tEz9izytF5Q10VGTSD0hs8fddA6QimGey52A2eFtvd/Df0Uro5aKUSwmY1S68jkBEm9SGFddHO+WOw+YLd0wXCw2DzBb2ljCjd9StS5Czja1xCDC8lwAP2HcomozNgZ5u/A7lYbpXPlDyeDcfCSuG6JZ3KqW5dnUVFYzQmbogybVXC8ZFnNpv8Ay9i/27BctNNJPK6aZ7pJHkuc95uXH3TyYP3QySeSrQrjDojdfO2WZB6V7XMlp5DYSWc0/wC14vY/oSD979kA48pBBCY4SJJyTdUIh5K2olpW0j5nGFhu1p/L9jyhwzyROBY5wc03BBtn3QwL88J7gcIBDRTSRYY8bTkMJxf3HcH5GVpN16ZzQ2upaauYRYmpZ+Lb4kFndrXNyM5yVlU7epJtOVqTUTW0+4cgJJTUSsKnNZQKo1CjkaelQyRF3YVRcOb92rO3mxAuB2CTxZxBUU6JNYLcPoVeb1FWIeECb1FKuxn0DCSSZOIJTZyoKTOUGYO70hSicoO9IUo+UBi0HYQHog4QnoBIDlGbwgDlHj4K0jRIO9RSSPKSxgU83VeSgFOCnI9kyWBW2+yUPqC1Xn+GWVD6wtKc2gCnPtFYfVmZIblQHKk7lJgu66p4JeSxTsuQtL6ltHBf1OPDflUGkDaG8/CfUd0cohkBD2AXB9yptbnyUztRXmlfLJ1JCS//AAnkc3cSwWFhhQbjnKiSqLjhEnzyyzK6OWKN0Y2yNbaRvvbgj/kf1VZMHEG4JB+FNr2PeOqwgdy3lEAP7qbBc27IlLTGoftBwPVfgI08TIAWs5HulclnBRQeMsqkqKSSYQPQ/wA9q3ao/wAIb+yxNOH462q8gUhv7Lju+6PR0y/4yZzz+T91FSf6iorr8HnPstQHCDP6kSEoc3qSrsZ9AkkgknFEnbymTtOVjFg+gJR8pH0BKP1JEMWBwhPRRwhPWMDHKNHwgjlHi4K0gxIu9RSSIyUlglXbt5TjhEqC3sgtNkwhKO7XK3PIDGAqZOVJz7hBoKeAZU4mPkdtZa5xlDVukd0huA810fAqZq1IpKSOKGjid1g0GSaQ3N7cD4Wdq05qawzfmc1t/uBZElk3ne4qrOd2LdvLbupxzEq1kAmRJIXxi7ghqieSbWBWTgZTsG5wFwL++E8jdri0EG3cd0RWHin6MW1vPugPe5xJJUC5IJcLOSm54wJJSskRwmFwWtNb+NdaGovtT2PdQ0endfcQm1o7SGrleJWnfHMKP9Mh/KinKZdJwMsQlQm5Tw8ppeUF2HwCCSSSYUSQSSasYsfkCdnKYHypM9SRDBxwoPUxwoPRMCHKPFwgd0aM4CEgxHPJSTO5KdDASiXE8pk9krKhMYcosTDI9rG8uNlDaluLcoMzzjg0tT0l+nMhkdKyRsgxt7H2KosftyVbdXdah+mqg7c07mPBH91RblyEc+SNG/bifZN0hcccFaGlxN6m4i5+VSbGXG61dNj2nKnbL4nZWnuB6qM3CyRzYrZ1dYvBuEafqC5fIIIyWAlmDwRlQW94c1HTNJqxPqNE6vikp3MdATt6b74IPcd1i1Lmvmkexu0OcSG+wVvBDPIBSBUU7eUo6JpwPMFLbhRJsfssE3aGqZFHyOFl6jN1piVXEhta6gSpQqUZbi9moc4KJNjNykYD7KdICXD3WqKcFvCE57WLCG4yWxkZQpeVpyxbAcLNl5KaEtws4bQSZOmVSQkm8pJwgYM3hJpymb6UgcoDB2nCi5RD8JFyxske6IzhCupNdZZmQQ8pIZeksHIEcqdhZJJMKIBRcEkkDMY8KUfqSSQfRvJdhAxhaFOSALJJLns6Ousr6kSRlZB5SSVKeiFv2GJJ5JTt9CSSuRIqTOUkkoyDILuUkljMYKTRdJJYxo0MbQWlbAGAkkuDUdnfR9SnWd1izeopJK9HRG/sEmSSXScok6ZJYwVvpTd0kkoWSSSSRMMVElMkiAV0ySSwT//Z",
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
                "data:image/png;base64,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",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSymZkmgQ77xxYPLHnd8aqrZ3JNU9TaIw9qBg&s",
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