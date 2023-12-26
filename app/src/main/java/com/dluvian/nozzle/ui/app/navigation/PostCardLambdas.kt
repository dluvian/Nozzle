package com.dluvian.nozzle.ui.app.navigation

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.deletor.INoteDeletor
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey

@Immutable
data class PostCardLambdas(
    val navLambdas: PostCardNavLambdas,
    val onLike: (PostWithMeta) -> Unit,
    val onDeleteLike: (NoteId) -> Unit,
    val onFollow: (Pubkey) -> Unit,
    val onUnfollow: (Pubkey) -> Unit,
    val onShowMedia: (String) -> Unit,
    val onShouldShowMedia: (String) -> Boolean,
    val onDelete: (NoteId) -> Unit,
) {
    companion object {
        fun create(
            navLambdas: PostCardNavLambdas,
            postCardInteractor: IPostCardInteractor,
            noteDeletor: INoteDeletor,
            profileFollower: IProfileFollower,
            clickedMediaUrlCache: IClickedMediaUrlCache
        ): PostCardLambdas {
            return PostCardLambdas(
                navLambdas = navLambdas,
                onLike = { post ->
                    postCardInteractor.like(
                        noteId = post.entity.id,
                        postPubkey = post.pubkey
                    )
                },
                onDeleteLike = { noteId ->
                    postCardInteractor.deleteLike(noteId = noteId)
                },
                onFollow = { pubkeyToFollow ->
                    profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
                },
                onUnfollow = { pubkeyToUnfollow ->
                    profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
                },
                onShowMedia = { mediaUrl ->
                    clickedMediaUrlCache.insert(mediaUrl)
                },
                onShouldShowMedia = { mediaUrl ->
                    clickedMediaUrlCache.contains(mediaUrl)
                },
                onDelete = { noteId -> noteDeletor.deleteNote(noteId = noteId) }
            )
        }
    }
}
