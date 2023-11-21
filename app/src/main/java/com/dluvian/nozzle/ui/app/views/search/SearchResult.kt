package com.dluvian.nozzle.ui.app.views.search

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.model.SimpleProfile

sealed class SearchResult(val text: AnnotatedString)

class ProfileSearchResult(val profile: SimpleProfile) :
    SearchResult(text = AnnotatedString(profile.name))

class HashtagSearchResult(val hashtag: String) :
    SearchResult(text = AnnotatedString(hashtag))

class NoteSearchResult(val id: String, val authorName: String, preview: AnnotatedString) :
    SearchResult(text = preview)