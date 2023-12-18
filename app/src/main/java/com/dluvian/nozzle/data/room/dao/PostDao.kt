package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended
import com.dluvian.nozzle.data.utils.escapeSQLPercentChars
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.nostr.Event
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
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedBasePostsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
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
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getAuthoredFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
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
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND (:isOnlyMention = 0 OR (id IN (SELECT eventId FROM mention WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "  AND pubkey != (SELECT pubkey FROM account WHERE isActive = 1)" +
                ")) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedBasePostsByRelays(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        relays: Collection<String>,
        until: Long,
        limit: Int,
        isOnlyMention: Boolean = false,
    ): List<PostEntity>

    /**
     * Sorted from newest to oldest
     */
    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND (:isOnlyLikedByMe = 0 OR (id IN (SELECT eventId FROM reaction WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)))) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        until: Long,
        limit: Int,
        isOnlyLikedByMe: Boolean = false,
    ): List<PostEntity>


    // TODO: Determine mentionedPubkey via db table
    suspend fun getInboxBasePosts(
        until: Long,
        limit: Int,
        relays: Collection<String>
    ): List<PostEntity> {
        return getGlobalFeedBasePostsByRelays(
            isPosts = true,
            isReplies = true,
            hashtag = null,
            relays = relays,
            until = until,
            limit = limit,
            isOnlyMention = true,
        )
    }

    suspend fun getLikedPosts(until: Long, limit: Int): List<PostEntity> {
        return getGlobalFeedBasePosts(
            isPosts = true,
            isReplies = true,
            hashtag = null,
            until = until,
            limit = limit,
            isOnlyLikedByMe = true
        )
    }

    @Query(
        // SELECT PostEntity
        "SELECT mainPost.*, " +
                // SELECT likedByMe
                "(SELECT eventId IS NOT NULL " +
                "FROM reaction " +
                "WHERE eventId = mainPost.id AND pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "AS isLikedByMe, " +
                // SELECT name
                "mainProfile.name, " +
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
    fun listExtendedPostsFlow(postIds: Collection<String>): Flow<List<PostEntityExtended>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg posts: PostEntity)

    @Transaction
    suspend fun insertWithHashtagsAndMentions(
        events: Collection<Event>,
        hashtagDao: HashtagDao,
        mentionDao: MentionDao,
    ) {
        val postEvents = events.filter { it.isPost() }
        if (postEvents.isEmpty()) return

        val posts = postEvents.map { PostEntity.fromEvent(it) }
        insertOrIgnore(*posts.toTypedArray())

        val hashtags = postEvents.flatMap { HashtagEntity.fromEvent(it) }
        if (hashtags.isNotEmpty()) {
            hashtagDao.insertOrIgnore(*hashtags.toTypedArray())
        }

        val mentions = postEvents.flatMap { MentionEntity.fromEvent(it) }
        if (mentions.isNotEmpty()) {
            mentionDao.insertOrIgnore(*mentions.toTypedArray())
        }
    }

    @Query("SELECT * FROM post WHERE id = :id")
    suspend fun getPost(id: String): PostEntity?

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE replyToId = :currentPostId " + // All replies to current post
                "OR id = :currentPostId" // Current post
    )
    suspend fun getPostAndReplies(currentPostId: String): List<PostEntity>

    @MapInfo(keyColumn = "id")
    @Query(
        "SELECT id, post.pubkey, content, name, picture, post.createdAt " +
                "FROM post " +
                "LEFT JOIN profile ON post.pubkey = profile.pubkey " +
                "WHERE id IN (:postIds) "
    )
    fun getMentionedPostsByIdFlow(postIds: Collection<String>): Flow<Map<String, MentionedPost>>

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
                "WHERE id NOT IN (:exclude) " +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND id NOT IN (" +
                // Exclude newest without the ones already excluded
                "SELECT id FROM post WHERE id NOT IN (:exclude) " +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "ORDER BY createdAt DESC LIMIT :amountToKeep" +
                ")"
    )
    suspend fun deleteAllExceptNewest(
        amountToKeep: Int,
        exclude: Collection<String>,
    ): Int

    @Query(
        "SELECT pubkey " +
                "FROM post " +
                "WHERE id IN (:postIds) " +
                "AND pubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun getUnknownAuthors(postIds: Collection<String>): List<String>

    // UNION ALL retains order
    @Query(
        "SELECT * FROM post WHERE content LIKE :start ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT * FROM post WHERE content LIKE :somewhere ESCAPE '\\' " +
                "LIMIT :limit"
    )
    suspend fun internalGetPostsWithSimilarContent(
        start: String,
        somewhere: String,
        limit: Int
    ): List<PostEntity>

    suspend fun getPostsWithSimilarContent(content: String, limit: Int): List<PostEntity> {
        val fixedContent = content.escapeSQLPercentChars()
        return internalGetPostsWithSimilarContent(
            start = "$fixedContent%",
            somewhere = "%$fixedContent%",
            limit = limit
        )
    }

    @Query("DELETE FROM post WHERE id = :postId")
    suspend fun deletePost(postId: NoteId)
}
