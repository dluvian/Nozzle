package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.runtime.Immutable

@Immutable
data class FilterCategory(
    val name: String,
    val filters: List<TypedFilterValue>,
    val onAdd: (() -> Unit)? = null
)
