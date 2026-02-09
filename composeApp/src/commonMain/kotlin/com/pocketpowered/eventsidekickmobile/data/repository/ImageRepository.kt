package com.district37.toastmasters.data.repository

import com.apollographql.apollo.ApolloClient
import com.district37.toastmasters.graphql.CreateImageMutation
import com.district37.toastmasters.graphql.DeleteImageMutation
import com.district37.toastmasters.graphql.UpdateImageMutation
import com.district37.toastmasters.graphql.type.CreateImageInput
import com.district37.toastmasters.graphql.type.FocusRegionInput
import com.district37.toastmasters.graphql.type.UpdateImageInput
import com.apollographql.apollo.api.Optional
import com.district37.toastmasters.data.transformers.toImage
import com.district37.toastmasters.models.FocusRegion
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.util.Resource

/**
 * Repository for Image data
 * Handles creating, updating, and deleting images
 */
class ImageRepository(apolloClient: ApolloClient) : BaseRepository(apolloClient) {

    override val tag = "ImageRepository"

    /**
     * Create a new image and link it to an entity
     *
     * @param input The image creation input containing url, altText, caption, entityType, and entityId
     * @return Resource containing the created Image on success
     */
    suspend fun createImage(input: CreateImageInput): Resource<Image> {
        return executeMutation(
            mutationName = "createImage(url=${input.url}, entityType=${input.entityType}, entityId=${input.entityId})",
            mutation = {
                apolloClient.mutation(CreateImageMutation(input = input)).execute()
            },
            transform = { data ->
                data.createImage.imageDetails.toImage()
            }
        )
    }

    /**
     * Delete an image by ID
     *
     * @param imageId The ID of the image to delete
     * @return Resource containing true on success
     */
    suspend fun deleteImage(imageId: Int): Resource<Boolean> {
        return executeMutation(
            mutationName = "deleteImage(id=$imageId)",
            mutation = {
                apolloClient.mutation(DeleteImageMutation(id = imageId)).execute()
            },
            transform = { data ->
                data.deleteImage
            }
        )
    }

    /**
     * Update an image's focus region
     *
     * @param imageId The ID of the image to update
     * @param focusRegion The new focus region
     * @return Resource containing the updated Image on success
     */
    suspend fun updateImageFocusRegion(imageId: Int, focusRegion: FocusRegion): Resource<Image> {
        val focusRegionInput = FocusRegionInput(
            x = focusRegion.x.toDouble(),
            y = focusRegion.y.toDouble(),
            width = focusRegion.width.toDouble(),
            height = focusRegion.height.toDouble()
        )

        val updateInput = UpdateImageInput(
            focusRegion = Optional.present(focusRegionInput)
        )

        return executeMutation(
            mutationName = "updateImage(id=$imageId, focusRegion updated)",
            mutation = {
                apolloClient.mutation(UpdateImageMutation(id = imageId, input = updateInput)).execute()
            },
            transform = { data ->
                data.updateImage.imageDetails.toImage()
            }
        )
    }
}
