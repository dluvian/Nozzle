package com.dluvian.nozzle.ui.app.navigation

import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey

data class PostCardLambdas(
    val navLambdas: PostCardNavLambdas,
    val onLike: (PostWithMeta) -> Unit,
    val onFollow: (Pubkey) -> Unit,
    val onUnfollow: (Pubkey) -> Unit,
    val onShowMedia: (String) -> Unit,
    val onShouldShowMedia: (String) -> Boolean,
) {
    companion object {
        fun create(
            navLambdas: PostCardNavLambdas,
            postCardInteractor: IPostCardInteractor,
            profileFollower: IProfileFollower,
            clickedMediaUrlCache: IClickedMediaUrlCache
        ): PostCardLambdas {
            return PostCardLambdas(
                navLambdas = navLambdas,
                onLike = { post ->
                    postCardInteractor.like(
                        postId = post.entity.id,
                        postPubkey = post.pubkey
                    )
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
                }
            )
        }
    }
}
