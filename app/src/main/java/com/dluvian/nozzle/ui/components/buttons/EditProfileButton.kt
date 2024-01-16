package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R

@Composable
fun EditProfileButton(onNavToEditProfile: () -> Unit) {
    Button(
        onClick = onNavToEditProfile,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground),
    ) {
        Text(text = stringResource(id = R.string.edit))
    }
}
