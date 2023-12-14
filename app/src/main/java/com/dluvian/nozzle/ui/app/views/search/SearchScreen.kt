package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.ui.app.navigation.PostCardNavLambdas
import com.dluvian.nozzle.ui.components.ChangeableTextField
import com.dluvian.nozzle.ui.components.ReturnableTopBar
import com.dluvian.nozzle.ui.components.SearchTopBarButton
import com.dluvian.nozzle.ui.components.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.itemRow.ItemRow
import com.dluvian.nozzle.ui.components.itemRow.PictureAndName
import com.dluvian.nozzle.ui.components.postCard.PostCard
import com.dluvian.nozzle.ui.theme.hintGray
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    profileSearchResult: List<SimpleProfile>,
    postSearchResult: List<PostWithMeta>,
    postCardNavLambdas: PostCardNavLambdas,
    onManualSearch: (String) -> Unit,
    onTypeSearch: (String) -> Unit,
    onChangeSearchType: (SearchType) -> Unit,
    onResetUI: () -> Unit,
    onSubscribeUnknownContacts: () -> Unit,
    onLike: (PostWithMeta) -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onShowMedia: (String) -> Unit,
    onShouldShowMedia: (String) -> Boolean,
    onFollow: (Pubkey) -> Unit,
    onUnfollow: (Pubkey) -> Unit,
    onNavigateToId: (String) -> Unit,
    onNavigateToProfile: (Pubkey) -> Unit,
    onGoBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val input = remember { mutableStateOf(TextFieldValue()) }
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.search),
            onGoBack = onGoBack,
            trailingIcon = {
                if (uiState.isLoading) TopBarCircleProgressIndicator()
                else SearchTopBarButton(
                    hasChanges = input.value.text.isNotBlank(),
                    onSearch = { onManualSearch(input.value.text) },
                )
            }
        )
        SearchBar(
            input = input,
            isInvalidNip05 = uiState.isInvalidNip05,
            focusRequester = focusRequester,
            onManualSearch = { onManualSearch(input.value.text) },
            onTypeSearch = { onTypeSearch(input.value.text) },
        )
        val currentSearchType = remember(uiState.searchType) { mutableStateOf(uiState.searchType) }
        SelectionOptions(
            searchType = currentSearchType.value,
            onChangeSearchType = { newSearchType ->
                onChangeSearchType(newSearchType)
                onTypeSearch(input.value.text)
            })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (currentSearchType.value) {
                SearchType.PEOPLE -> {
                    items(profileSearchResult) {
                        ItemRow(content = {
                            PictureAndName(
                                profile = it,
                                onNavigateToProfile = onNavigateToProfile
                            )
                        }, onClick = { onNavigateToProfile(it.pubkey) })
                    }
                }

                SearchType.NOTES -> {
                    items(postSearchResult) {
                        ItemRow(content = {
                            PostCard(
                                post = it,
                                postCardNavLambdas = postCardNavLambdas,
                                onLike = { onLike(it) },
                                onPrepareReply = onPrepareReply,
                                onShowMedia = onShowMedia,
                                onShouldShowMedia = onShouldShowMedia,
                                isCurrent = true,
                                onFollow = onFollow,
                                onUnfollow = onUnfollow
                            )
                        }, onClick = { onNavigateToProfile(it.pubkey) })
                    }
                }
            }
        }
    }
    val finalIdIsNotEmpty = remember(uiState.finalId) { uiState.finalId.isNotEmpty() }
    if (finalIdIsNotEmpty) onNavigateToId(uiState.finalId)


    DisposableEffect(true) {
        onDispose { onResetUI() }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        onSubscribeUnknownContacts()
    }
}

@Composable
private fun SearchBar(
    input: MutableState<TextFieldValue>,
    isInvalidNip05: Boolean,
    focusRequester: FocusRequester,
    onManualSearch: () -> Unit,
    onTypeSearch: () -> Unit,
) {
    ChangeableTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        isError = isInvalidNip05,
        input = input,
        maxLines = Int.MAX_VALUE,
        placeholder = stringResource(id = R.string.search_profiles),
        errorLabel = if (isInvalidNip05) stringResource(id = R.string.failed_to_resolve_nip05)
        else null,
        keyboardImeAction = ImeAction.Search,
        onChangeInput = { onTypeSearch() },
        onImeAction = onManualSearch,
    )
}

@Composable
private fun SelectionOptions(searchType: SearchType, onChangeSearchType: (SearchType) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = spacing.screenEdge)) {
        SelectionItem(
            text = stringResource(id = R.string.people),
            icon = Icons.Rounded.Person,
            isSelected = searchType == SearchType.PEOPLE,
            onClick = { onChangeSearchType(SearchType.PEOPLE) }
        )
        Spacer(modifier = Modifier.width(spacing.screenEdge))
        SelectionItem(
            text = stringResource(id = R.string.notes),
            icon = Icons.Rounded.Description,
            isSelected = searchType == SearchType.NOTES,
            onClick = { onChangeSearchType(SearchType.NOTES) }
        )
    }
}

@Composable
fun SelectionItem(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colors.onBackground else MaterialTheme.colors.hintGray
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            backgroundColor = MaterialTheme.colors.background
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, color = color)
    }
}
