package com.dluvian.nozzle.ui.components.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.drawerFilter.CheckBoxFilterValue
import com.dluvian.nozzle.model.drawerFilter.FilterCategory
import com.dluvian.nozzle.model.drawerFilter.SwitchFilterValue
import com.dluvian.nozzle.model.drawerFilter.TypedFilterValue
import com.dluvian.nozzle.ui.components.namedItem.NamedCheckbox
import com.dluvian.nozzle.ui.components.namedItem.NamedSwitch
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
                                    Text(text = stringResource(id = R.string.apply_and_close))
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
    HorizontalDivider()
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
