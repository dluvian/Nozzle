package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SearchIcon

@Composable
fun SearchIconButton(onSearch: () -> Unit, description: String) {
    BaseIconButton(imageVector = SearchIcon, description = description, onClick = onSearch)
}
