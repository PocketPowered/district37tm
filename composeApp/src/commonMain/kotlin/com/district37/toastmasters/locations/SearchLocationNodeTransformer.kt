package com.district37.toastmasters.locations

import com.district37.toastmasters.graphql.SearchLocationsQuery
import com.district37.toastmasters.models.Location
import com.wongislandd.nexus.util.Transformer

class SearchLocationNodeTransformer : Transformer<SearchLocationsQuery.Node, Location> {
    override fun transform(input: SearchLocationsQuery.Node): Location {
        return Location(
            id = input.id,
            locationName = input.location_name,
            locationImages = input.location_imagesFilterNotNull()
        )
    }
}
