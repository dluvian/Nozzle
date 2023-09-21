package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.helper.BasePost
import com.dluvian.nozzle.data.room.helper.ReplyContext
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended
import com.dluvian.nozzle.model.MentionedPost
import kotlinx.coroutines.flow.Flow


@Dao
interface PostDao {

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey, post.content " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedBasePostsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<BasePost>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey, post.content " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        until: Long,
        limit: Int,
    ): List<BasePost>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey, post.content " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedBasePostsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<BasePost>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey, post.content " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        until: Long,
        limit: Int,
    ): List<BasePost>

    @Query(
        // SELECT PostEntity
        "SELECT mainPost.*, " +
                // SELECT likedByMe
                "(SELECT eventId IS NOT NULL " +
                "FROM reaction " +
                "WHERE eventId = mainPost.id AND pubkey = :personalPubkey) " +
                "AS isLikedByMe, " +
                // SELECT name and picture
                "mainProfile.name, " +
                "mainProfile.picture AS pictureUrl, " +
                // SELECT numOfReplies
                "(SELECT COUNT(*) FROM post WHERE post.replyToId = mainPost.id) " +
                "AS numOfReplies, " +
                // SELECT replyToPubkey
                "(SELECT post.pubkey " +
                "FROM post " +
                "WHERE post.id = mainPost.replyToId) " +
                "AS replyToPubkey, " +
                // SELECT replyToName
                "(SELECT profile.name " +
                "FROM profile " +
                "JOIN post " +
                "ON (profile.pubkey = post.pubkey AND post.id = mainPost.replyToId)) " +
                "AS replyToName " +
                // PostEntity
                "FROM post AS mainPost " +
                // Join author
                "LEFT JOIN profile AS mainProfile " +
                "ON mainPost.pubkey = mainProfile.pubkey " +
                // Conditioned by ids
                "WHERE mainPost.id IN (:postIds) " +
                "ORDER BY createdAt DESC "
    )
    fun listExtendedPostsFlow(
        postIds: Collection<String>,
        personalPubkey: String
    ): Flow<List<PostEntityExtended>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotPresent(vararg post: PostEntity)

    @Query(
        "SELECT post.id, post.replyToId, post.pubkey, post.content " +
                "FROM post " +
                "WHERE id = :id "
    )
    suspend fun getReplyContext(id: String): ReplyContext?

    @Query(
        "SELECT post.id, post.replyToId, post.pubkey, post.content " +
                "FROM post " +
                "WHERE replyToId = :currentPostId " + // All replies to current post
                "OR id = :currentPostId" // Current post
    )
    suspend fun listReplyContext(currentPostId: String): List<ReplyContext>

    @MapInfo(keyColumn = "id")
    @Query(
        "SELECT id, post.pubkey, content, name, picture, post.createdAt " +
                "FROM post " +
                "LEFT JOIN profile ON post.pubkey = profile.pubkey " +
                "WHERE id IN (:postIds) "
    )
    fun getMentionedPostsMapFlow(postIds: Collection<String>): Flow<Map<String, MentionedPost>>

    @Query(
        "SELECT id, post.pubkey, content, name, picture, post.createdAt " +
                "FROM post " +
                "LEFT JOIN profile ON post.pubkey = profile.pubkey " +
                "WHERE id = :postId"
    )
    suspend fun getMentionedPost(postId: String): MentionedPost?

    @Query(
        "SELECT id " +
                "FROM post " +
                "WHERE id IN (:postIds)"
    )
    suspend fun filterExistingIds(postIds: Collection<String>): List<String>

    @Query(
        "DELETE FROM post " +
                "WHERE " +
                "id NOT IN (:exclude) " +
                "AND pubkey IS NOT :excludeAuthor " +
                "AND id NOT IN (SELECT id FROM post ORDER BY createdAt DESC LIMIT :amountToKeep)"
    )
    suspend fun deleteAllExceptNewest(
        amountToKeep: Int,
        exclude: Collection<String>,
        excludeAuthor: String
    ): Int

    @Query("SELECT COUNT(*) FROM post")
    suspend fun countPosts(): Int

    @Query(
        "SELECT pubkey " +
                "FROM post " +
                "WHERE id IN (:postIds) " +
                "AND pubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun getUnknownAuthors(postIds: Collection<String>): List<String>
}
