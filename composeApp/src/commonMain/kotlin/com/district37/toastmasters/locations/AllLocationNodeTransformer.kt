package com.district37.toastmasters.locations

import com.district37.toastmasters.graphql.AllLocationsQuery
import com.district37.toastmasters.models.Location
import com.wongislandd.nexus.util.Transformer

class AllLocationNodeTransformer : Transformer<AllLocationsQuery.Node, Location> {
    override fun transform(input: AllLocationsQuery.Node): Location {
        return Location(
            id = input.id,
            locationName = input.location_name,
            locationImages = input.location_imagesFilterNotNull()
        )
    }
}
