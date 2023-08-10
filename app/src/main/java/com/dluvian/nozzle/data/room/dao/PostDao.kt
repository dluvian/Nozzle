package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.helper.IdAndPubkey
import com.dluvian.nozzle.data.room.helper.ReplyContext
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.PostEntityExtended
import kotlinx.coroutines.flow.Flow


@Dao
interface PostDao {

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedIdsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<IdAndPubkey>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedIds(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        until: Long,
        limit: Int,
    ): List<IdAndPubkey>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedIdsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<IdAndPubkey>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT post.id, post.pubkey " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedIds(
        isPosts: Boolean,
        isReplies: Boolean,
        until: Long,
        limit: Int,
    ): List<IdAndPubkey>


    @Query(
        "SELECT post.replyToId " +
                "FROM post " +
                "WHERE id = :id "
    )
    suspend fun getReplyToId(id: String): String?

    @Query(
        // SELECT PostEntity
        "SELECT mainPost.*, " +
                // SELECT mentioned post
                "mentionedPost.pubkey AS mentionedPostPubkey, " +
                "mentionedPost.content AS mentionedPostContent, " +
                "mentionedPost.createdAt AS mentionedPostCreatedAt, " +
                "mentionedProfile.name AS mentionedPostName, " +
                "mentionedProfile.picture AS mentionedPostPicture, " +
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
                "(SELECT profile.pubkey " +
                "FROM profile " +
                "JOIN post " +
                "ON (profile.pubkey = post.pubkey AND post.id = mainPost.replyToId)) " +
                "AS replyToPubkey, " +
                // SELECT replyToName // TODO: replyToPubkey + replyToName as embedded ?
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
                // Join mentioned post
                "LEFT JOIN post AS mentionedPost " +
                "ON mainPost.mentionedPostId = mentionedPost.id " +
                // Join profile of mentioned post author
                "LEFT JOIN profile AS mentionedProfile " +
                "ON mentionedPost.pubkey = mentionedProfile.pubkey " +
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
        "SELECT post.id, post.replyToId, post.pubkey " +
                "FROM post " +
                "WHERE replyToId = :currentPostId " + // All replies to current post
                "OR id = :currentPostId" // Current post
    )
    suspend fun listReplyContext(currentPostId: String): List<ReplyContext>

    @Query(
        "SELECT pubkey " +
                "FROM post " +
                "WHERE id IN (:postIds) "
    )
    suspend fun listAuthorPubkeys(postIds: Collection<String>): List<String>

    @MapInfo(keyColumn = "id")
    @Query(
        "SELECT id, post.pubkey, content, name, picture, post.createdAt " +
                "FROM post " +
                "JOIN profile ON post.pubkey = profile.pubkey " +
                "WHERE id IN (:postIds) "
    )
    fun getMentionedPostsMapFlow(postIds: Collection<String>): Flow<Map<String, MentionedPost>>
}
