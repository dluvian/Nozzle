package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.ui.app.navigation.PostCardLambdas
import com.dluvian.nozzle.ui.components.chips.IconedFilterChip
import com.dluvian.nozzle.ui.components.iconButtons.GoBackIconButton
import com.dluvian.nozzle.ui.components.iconButtons.SearchIconButton
import com.dluvian.nozzle.ui.components.indicators.TopBarCircleProgressIndicator
import com.dluvian.nozzle.ui.components.input.ChangeableTextField
import com.dluvian.nozzle.ui.components.postCard.PostCard
import com.dluvian.nozzle.ui.components.rows.ItemRow
import com.dluvian.nozzle.ui.components.rows.PictureAndName
import com.dluvian.nozzle.ui.theme.spacing


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchViewModelState,
    postCardLambdas: PostCardLambdas,
    profileSearchResult: List<SimpleProfile>,
    postSearchResult: List<PostWithMeta>,
    onManualSearch: (String) -> Unit,
    onTypeSearch: (String) -> Unit,
    onChangeSearchType: (SearchType) -> Unit,
    onResetUI: () -> Unit,
    onSubscribeUnknownContacts: () -> Unit,
    onPrepareReply: (PostWithMeta) -> Unit,
    onGoBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val input = remember { mutableStateOf(TextFieldValue()) }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    SearchBar(
                        input = input,
                        isInvalidNip05 = uiState.isInvalidNip05,
                        focusRequester = focusRequester,
                        onManualSearch = { onManualSearch(input.value.text) },
                        onTypeSearch = { onTypeSearch(input.value.text) },
                    )
                },
                navigationIcon = { GoBackIconButton(onGoBack = onGoBack) },
                actions = {
                    if (uiState.isLoading) TopBarCircleProgressIndicator()
                    else if (input.value.text.isNotBlank()) {
                        SearchIconButton(
                            onSearch = { onManualSearch(input.value.text) },
                            description = stringResource(id = R.string.search)
                        )
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val currentSearchType =
                remember(uiState.searchType) { mutableStateOf(uiState.searchType) }
            SelectionOptions(
                searchType = currentSearchType.value,
                onChangeSearchType = { newSearchType ->
                    onChangeSearchType(newSearchType)
                    if (uiState.searchType != newSearchType) onTypeSearch(input.value.text)
                })
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (currentSearchType.value) {
                    SearchType.PEOPLE -> {
                        items(profileSearchResult) {
                            ProfileRow(
                                profile = it,
                                onNavigateToProfile = postCardLambdas.navLambdas.onNavigateToProfile
                            )
                        }
                    }

                    SearchType.NOTES -> {
                        items(postSearchResult) {
                            ItemRow(
                                modifier = Modifier.fillMaxWidth(),
                                content = {
                                    PostCard(
                                        post = it,
                                        postCardLambdas = postCardLambdas,
                                        onPrepareReply = onPrepareReply,
                                    )
                                },
                                onClick = { postCardLambdas.navLambdas.onNavigateToProfile(it.pubkey) })
                        }
                    }
                }
            }
        }
        val finalIdIsNotEmpty = remember(uiState.finalId) { uiState.finalId.isNotEmpty() }
        if (finalIdIsNotEmpty) postCardLambdas.navLambdas.onNavigateToId(uiState.finalId)

        DisposableEffect(true) {
            onDispose { onResetUI() }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            onSubscribeUnknownContacts()
            onTypeSearch("")
        }
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
        placeholder = stringResource(id = R.string.search_local_database),
        errorLabel = if (isInvalidNip05) stringResource(id = R.string.failed_to_resolve_nip05)
        else null,
        keyboardImeAction = ImeAction.Search,
        onChangeInput = { onTypeSearch() },
        onImeAction = onManualSearch,
        isTransparent = true,
        isSingleLine = true
    )
}

@Composable
private fun SelectionOptions(searchType: SearchType, onChangeSearchType: (SearchType) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = spacing.screenEdge)) {
        IconedFilterChip(
            text = stringResource(id = R.string.people),
            isSelected = searchType == SearchType.PEOPLE,
            onClick = { onChangeSearchType(SearchType.PEOPLE) },
            leadingIcon = Icons.Rounded.Person
        )
        Spacer(modifier = Modifier.width(spacing.screenEdge))
        IconedFilterChip(
            text = stringResource(id = R.string.notes),
            isSelected = searchType == SearchType.NOTES,
            onClick = { onChangeSearchType(SearchType.NOTES) },
            leadingIcon = Icons.Rounded.Description
        )
    }
}

@Composable
private fun ProfileRow(profile: SimpleProfile, onNavigateToProfile: (Pubkey) -> Unit) {
    ItemRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = spacing.medium,
                horizontal = spacing.screenEdge
            ),
        content = {
            PictureAndName(
                profile = profile,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        onClick = { onNavigateToProfile(profile.pubkey) }
    )
}
