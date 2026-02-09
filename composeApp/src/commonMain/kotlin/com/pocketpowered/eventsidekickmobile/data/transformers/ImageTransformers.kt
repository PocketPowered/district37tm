package com.district37.toastmasters.data.transformers

import com.district37.toastmasters.graphql.fragment.ImageDetails
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.Image

/**
 * Transforms GraphQL ImageDetails fragment to domain Image model
 */
fun ImageDetails.toImage(): Image {
    return Image(
        id = id,
        url = url,
        altText = altText,
        caption = caption,
        focusRegion = focusRegion?.let {
            FocusRegion(
                x = it.x.toFloat(),
                y = it.y.toFloat(),
                width = it.width.toFloat(),
                height = it.height.toFloat()
            )
        },
        archivedAt = archivedAt
    )
}
