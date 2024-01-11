package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.NamedCheckbox
import com.dluvian.nozzle.ui.components.NamedSwitch
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun FilterDrawer(
    drawerState: DrawerState,
    filterCategories: List<FilterCategory>,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ModalDrawerSheet {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.screenEdge),
                            horizontalAlignment = Alignment.Start
                        ) {
                            items(filterCategories) { category ->
                                Category(category = category)
                            }
                            item {
                                Button(modifier = Modifier.fillMaxWidth(), onClick = onClose) {
                                    Text(text = "Apply and close")
                                }
                            }
                        }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                content()
            }
        }
    }
}

@Composable
private fun Category(category: FilterCategory) {
    Text(
        text = category.name,
        style = MaterialTheme.typography.titleMedium
    )
    category.filters.forEach { filter ->
        NamedCheckBoxOrSwitch(filter = filter)
    }
    category.onAdd?.let { onAdd ->
        TextButton(onClick = onAdd) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.add_relay_set)
            )
            Text(text = stringResource(id = R.string.add_relay_set))
        }
    }
    Divider()
    Spacer(modifier = Modifier.height(spacing.screenEdge))
}

@Composable
private fun NamedCheckBoxOrSwitch(filter: TypedFilterValue) {
    when (filter) {
        is CheckBoxFilterValue -> NamedCheckbox(
            isChecked = filter.isChecked,
            name = filter.name,
            isEnabled = filter.isEnabled,
            onClick = filter.onClick
        )

        is SwitchFilterValue -> NamedSwitch(
            isChecked = filter.isChecked,
            name = filter.name,
            isEnabled = filter.isEnabled,
            onClick = filter.onClick
        )
    }
}
