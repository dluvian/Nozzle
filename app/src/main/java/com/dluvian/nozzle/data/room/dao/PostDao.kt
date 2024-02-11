package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.RepostEntity
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import com.dluvian.nozzle.data.utils.escapeSQLPercentChars
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.nostr.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Dao
interface PostDao {

    suspend fun getMainFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>?,
        relays: Collection<String>?,
        until: Long,
        limit: Int,
    ): List<PostEntity> {
        // TODO: Combine it into single query.
        //  Currently not possible because the null check will make the query too long
        return if (authorPubkeys == null && relays == null) {
            internalGetGlobalFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                until = until,
                limit = limit
            )
        } else if (!authorPubkeys.isNullOrEmpty() && relays == null) {
            internalGetAuthoredGlobalFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                authorPubkeys = authorPubkeys,
                until = until,
                limit = limit
            )
        } else if (authorPubkeys == null && !relays.isNullOrEmpty()) {
            internalGetRelayedGlobalFeedBasePosts(
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                relays = relays,
                until = until,
                limit = limit
            )
        } else {
            if (authorPubkeys?.isEmpty() == true || relays?.isEmpty() == true) emptyList()
            else if (authorPubkeys != null && relays != null)
                internalGetMainFeedBasePosts(
                    isPosts = isPosts,
                    isReplies = isReplies,
                    hashtag = hashtag,
                    authorPubkeys = authorPubkeys,
                    relays = relays,
                    until = until,
                    limit = limit
                )
            else emptyList()
        }
    }

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun internalGetMainFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun internalGetGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun internalGetAuthoredGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND createdAt < :until " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun internalGetRelayedGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>


    fun getNumOfNewMainFeedPostsFlow(
        oldPostIds: List<String>,
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>?,
        relays: Collection<String>?,
        limit: Int,
    ): Flow<Int> {
        return if (authorPubkeys == null && relays == null) {
            internalGetNumOfNewGlobalFeedPostsFlow(
                oldPostIds = oldPostIds,
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                limit = limit,
            )
        } else if (!authorPubkeys.isNullOrEmpty() && relays == null) {
            internalGetNumOfNewAuthoredGlobalFeedPostsFlow(
                oldPostIds = oldPostIds,
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                authorPubkeys = authorPubkeys,
                limit = limit
            )
        } else if (authorPubkeys == null && !relays.isNullOrEmpty()) {
            internalGetNumOfNewRelayedGlobalPostsFlow(
                oldPostIds = oldPostIds,
                isPosts = isPosts,
                isReplies = isReplies,
                hashtag = hashtag,
                relays = relays,
                limit = limit
            )
        } else {
            if (authorPubkeys?.isEmpty() == true || relays?.isEmpty() == true) flowOf(0)
            else if (authorPubkeys != null && relays != null)
                internalGetNumOfNewMainFeedPostsFlow(
                    oldPostIds = oldPostIds,
                    isPosts = isPosts,
                    isReplies = isReplies,
                    hashtag = hashtag,
                    authorPubkeys = authorPubkeys,
                    relays = relays,
                    limit = limit
                )
            else flowOf(0)
        }
    }

    @Query(
        "SELECT COUNT(*) " +
                "FROM (" +
                "SELECT id " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun internalGetNumOfNewMainFeedPostsFlow(
        oldPostIds: List<String>,
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>,
        relays: Collection<String>,
        limit: Int,
    ): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM (SELECT id " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun internalGetNumOfNewGlobalFeedPostsFlow(
        oldPostIds: List<String>,
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        limit: Int
    ): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM (SELECT id " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND pubkey IN (:authorPubkeys) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun internalGetNumOfNewAuthoredGlobalFeedPostsFlow(
        oldPostIds: List<String>,
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: Collection<String>,
        limit: Int
    ): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM (SELECT id " +
                "FROM post " +
                "WHERE ((:isReplies AND replyToId IS NOT NULL) OR (:isPosts AND replyToId IS NULL)) " +
                "AND (:hashtag IS NULL OR id IN (SELECT eventId FROM hashtag WHERE hashtag = :hashtag)) " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun internalGetNumOfNewRelayedGlobalPostsFlow(
        oldPostIds: List<String>,
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        relays: Collection<String>,
        limit: Int,
    ): Flow<Int>

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE createdAt < :until " +
                "AND (id IN (SELECT eventId FROM mention WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "  AND pubkey != (SELECT pubkey FROM account WHERE isActive = 1)" +
                ") " +
                "AND id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getInboxPosts(
        relays: Collection<String>,
        until: Long,
        limit: Int,
    ): List<PostEntity>

    @Query(
        "SELECT COUNT(*) " +
                "FROM (" +
                "SELECT id " +
                "FROM post " +
                "WHERE id IN (SELECT DISTINCT eventId FROM eventRelay WHERE relayUrl IN (:relays)) " +
                "AND (id IN (SELECT eventId FROM mention WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "  AND pubkey != (SELECT pubkey FROM account WHERE isActive = 1)" +
                ") " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun getNumOfNewInboxPostsFlow(
        oldPostIds: List<String>,
        relays: Collection<String>,
        limit: Int
    ): Flow<Int>

    @Query(
        "SELECT * " +
                "FROM post " +
                "WHERE createdAt < :until " +
                "AND id IN (SELECT eventId FROM reaction WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getLikedPosts(
        until: Long,
        limit: Int,
    ): List<PostEntity>

    @Query(
        "SELECT COUNT(*) " +
                "FROM (" +
                "SELECT id " +
                "FROM post " +
                "WHERE id IN (SELECT eventId FROM reaction WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "ORDER BY createdAt DESC " +
                "LIMIT :limit" +
                ") " +
                "WHERE id NOT IN (:oldPostIds)"
    )
    fun getNumOfNewLikedPostsFlow(
        oldPostIds: List<String>,
        limit: Int
    ): Flow<Int>

    @Query(
        // SELECT PostEntity
        "SELECT mainPost.*, " +
                // SELECT likedByMe
                "(SELECT eventId IS NOT NULL " +
                "FROM reaction " +
                "WHERE eventId = mainPost.id AND pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "AS isLikedByMe, " +
                // SELECT name and picture
                "mainProfile.name, mainProfile.picture, " +
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

    @Transaction
    suspend fun insertRepost(events: Collection<Event>, repostDao: RepostDao) {
        val reposts = events
            .filter { it.isRepost() }
            .mapNotNull {
                val repostedId = it.getRepostedId() ?: return@mapNotNull null
                val relay = it.getRepostedRelayUrlHint()
                val neventUri = EncodingUtils.createNeventUri(
                    postId = repostedId,
                    relays = if (relay.isNullOrBlank() || !relay.isWebsocketUrl()) emptyList()
                    else listOf(relay)
                ) ?: return@mapNotNull null

                PostEntity
                    .fromEvent(it)
                    .copy(content = neventUri, replyToId = null, replyRelayHint = null)
            }
        if (reposts.isEmpty()) return

        val repostEntities = reposts.map { RepostEntity(eventId = it.id) }
        insertOrIgnore(*reposts.toTypedArray())
        repostDao.insertOrIgnore(*repostEntities.toTypedArray())
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
    @RewriteQueriesToDropUnusedColumns
    fun getMentionedPostsByIdFlow(postIds: Collection<String>): Flow<Map<String, MentionedPost>>

    @Query(
        "SELECT id, post.pubkey, content, name, picture, post.createdAt " +
                "FROM post " +
                "LEFT JOIN profile ON post.pubkey = profile.pubkey " +
                "WHERE id = :postId"
    )
    @RewriteQueriesToDropUnusedColumns
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
                "AND id NOT IN (SELECT replyToId WHERE pubkey IN (SELECT pubkey FROM account) AND replyToId NOT NUll) " +
                "AND id NOT IN (SELECT id FROM post ORDER BY createdAt DESC LIMIT :amountToKeep)"
    )
    suspend fun deleteExceptNewestAndOwn(
        amountToKeep: Int,
        exclude: Collection<String>,
    ): Int

    @Query(
        "DELETE FROM post WHERE id IN (" +
                "SELECT id FROM post WHERE pubkey IN (SELECT pubkey FROM account) " +
                "ORDER BY createdAt DESC LIMIT :amountToKeep)"
    )
    suspend fun deleteOwnPosts(amountToKeep: Int): Int

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
