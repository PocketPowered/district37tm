package com.district37.toastmasters.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.AnyAdapter
import com.apollographql.apollo3.api.LongAdapter
import com.apollographql.apollo3.api.StringAdapter
import com.district37.toastmasters.graphql.type.BigInt
import com.district37.toastmasters.graphql.type.JSON
import com.district37.toastmasters.graphql.type.UUID

private const val SUPABASE_URL = "https://yarbshxeeufpgquawcuy.supabase.co"
private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_pnaLVfP6H6Kxi5wCPhSO2A_aK4zo24t"

fun createSupabaseApolloClient(): ApolloClient {
    return ApolloClient.Builder()
        .serverUrl("$SUPABASE_URL/graphql/v1")
        .addHttpHeader("apikey", SUPABASE_PUBLISHABLE_KEY)
        .addHttpHeader("Authorization", "Bearer $SUPABASE_PUBLISHABLE_KEY")
        .addCustomScalarAdapter(BigInt.type, LongAdapter)
        .addCustomScalarAdapter(JSON.type, AnyAdapter)
        .addCustomScalarAdapter(UUID.type, StringAdapter)
        .build()
}
