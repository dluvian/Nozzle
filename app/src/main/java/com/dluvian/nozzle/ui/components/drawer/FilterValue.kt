package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.runtime.Immutable

sealed interface TypedFilterValue {
    val name: String
    val isChecked: Boolean
    val values: List<String>?
    val onClick: () -> Unit
}

@Immutable
data class CheckBoxFilterValue(
    override val name: String,
    override val isChecked: Boolean,
    override val values: List<String>? = null,
    override val onClick: () -> Unit
) : TypedFilterValue


@Immutable
data class SwitchFilterValue(
    override val name: String,
    override val isChecked: Boolean,
    override val values: List<String>? = null,
    override val onClick: () -> Unit
) : TypedFilterValue
