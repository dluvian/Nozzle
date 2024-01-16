package com.dluvian.nozzle.model.drawerFilter

import androidx.compose.runtime.Immutable

sealed interface TypedFilterValue {
    val name: String
    val isSelected: Boolean
    val isEnabled: Boolean
    val onClick: () -> Unit
}

@Immutable
data class CheckBoxFilterValue(
    override val name: String,
    override val isSelected: Boolean,
    override val isEnabled: Boolean = true,
    override val onClick: () -> Unit
) : TypedFilterValue


@Immutable
data class RadioFilterValue(
    override val name: String,
    override val isSelected: Boolean,
    override val isEnabled: Boolean = true,
    override val onClick: () -> Unit
) : TypedFilterValue
