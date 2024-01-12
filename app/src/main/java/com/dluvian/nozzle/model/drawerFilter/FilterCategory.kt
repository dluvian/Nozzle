package com.dluvian.nozzle.model.drawerFilter

import androidx.compose.runtime.Immutable

@Immutable
data class FilterCategory(
    val name: String,
    val filters: List<TypedFilterValue>,
)
