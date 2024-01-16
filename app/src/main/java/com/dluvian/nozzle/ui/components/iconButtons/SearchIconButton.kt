package com.dluvian.nozzle.ui.components.iconButtons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dluvian.nozzle.ui.theme.SearchIcon

@Composable
fun SearchIconButton(onSearch: () -> Unit, description: String) {
    IconButton(onClick = onSearch) {
        Icon(imageVector = SearchIcon, contentDescription = description)
    }
}
