package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.PostEntityExtended
import kotlinx.coroutines.flow.Flow


@Dao
interface PostDao {

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeed(
        isPosts: Boolean,
        isReplies: Boolean,
        authorPubkeys: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeed(
        isPosts: Boolean,
        isReplies: Boolean,
        until: Long,
        limit: Int,
    ): List<PostEntity>


    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE id = :id "
    )
    suspend fun getPost(id: String): PostEntity?

    @Query(
        // SELECT PostEntity
        "SELECT post1.*, " +
                // SELECT likedByMe
                "reaction.pubkey IS NOT NULL AS isLikedByMe, " +
                // SELECT name
                "profile1.name, " +
                // SELECT pictureUrl
                "profile1.picture AS pictureUrl, " +
                // SELECT numOfReplies
                "(SELECT COUNT(*) FROM post AS post2 WHERE post2.replyToId = post1.id) " +
                "AS numOfReplies, " +
                // SELECT replyToPubkey
                "(SELECT profile2.pubkey " +
                "FROM profile AS profile2 " +
                "JOIN post AS post3 " +
                "ON (profile2.pubkey = post3.pubkey AND post3.id = post1.replyToId)) " +
                "AS replyToPubkey, " +
                // SELECT replyToName
                "(SELECT profile3.name " +
                "FROM profile AS profile3 " +
                "JOIN post AS post4 " +
                "ON (profile3.pubkey = post4.pubkey AND post4.id = post1.replyToId)) " +
                "AS replyToName " +
                // PostEntity
                "FROM post AS post1 " +
                // Join my own reaction
                "LEFT JOIN reaction " +
                "ON (post1.id = reaction.eventId AND post1.pubkey = :personalPubkey) " +
                // Join author
                "LEFT JOIN profile AS profile1 " +
                "ON post1.pubkey = profile1.pubkey " +
                // Conditioned by ids
                "WHERE post1.id IN (:postIds) " +
                "ORDER BY createdAt DESC "
    )
    fun listExtendedPostsFlow(
        postIds: Collection<String>,
        personalPubkey: String
    ): Flow<List<PostEntityExtended>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotPresent(vararg post: PostEntity)

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE replyToId = :currentPostId " + // All replies to current post
                "OR id = :currentPostId " + // Current post
                "OR (:replyToId IS NOT NULL AND id = :replyToId) " // Direct parent
    )
    suspend fun getThreadEnd(currentPostId: String, replyToId: String?): List<PostEntity>

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
