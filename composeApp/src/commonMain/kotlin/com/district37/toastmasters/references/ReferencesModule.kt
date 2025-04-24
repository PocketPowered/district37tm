package com.district37.toastmasters.references

import com.district37.toastmasters.ReferencesRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val referencesModule = module {
    viewModelOf(::ReferencesViewModel)
    singleOf(::ReferencesRepository)
} 