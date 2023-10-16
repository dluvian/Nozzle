package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.helper.extended.PostEntityExtended
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.Pubkey
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
                "ORDER BY createdAt DESC " +
                "LIMIT :limit"
    )
    suspend fun getGlobalFeedBasePosts(
        isPosts: Boolean,
        isReplies: Boolean,
        hashtag: String?,
        until: Long,
        limit: Int,
    ): List<PostEntity>


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
        )
    }

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
    suspend fun insertIfNotPresent(post: PostEntity): Long

    @Transaction
    suspend fun insertWithHashtagsAndMentions(
        postEntity: PostEntity,
        hashtagDao: HashtagDao,
        hashtags: Collection<String>,
        mentionDao: MentionDao,
        mentions: Collection<Pubkey>
    ) {
        val inserted = insertIfNotPresent(postEntity)
        if (inserted == -1L) {
            return
        }

        if (hashtags.isNotEmpty()) {
            val entities = hashtags.map { HashtagEntity(eventId = postEntity.id, hashtag = it) }
            hashtagDao.insertOrIgnore(*entities.toTypedArray())
        }

        if (mentions.isNotEmpty()) {
            val entities = mentions.map { MentionEntity(eventId = postEntity.id, pubkey = it) }
            mentionDao.insertOrIgnore(*entities.toTypedArray())
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
                "WHERE " +
                "id NOT IN (:exclude) " +
                "AND pubkey IS NOT :excludeAuthor " +
                "AND id NOT IN (" +
                // Exclude newest without the ones already excluded
                "SELECT id " +
                "FROM post " +
                "WHERE id NOT IN (:exclude) " +
                "AND pubkey IS NOT :excludeAuthor " +
                "ORDER BY createdAt DESC " +
                "LIMIT :amountToKeep" +
                ")"
    )
    suspend fun deleteAllExceptNewest(
        amountToKeep: Int,
        exclude: Collection<String>,
        excludeAuthor: String
    ): Int

    @Query(
        "SELECT pubkey " +
                "FROM post " +
                "WHERE id IN (:postIds) " +
                "AND pubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun getUnknownAuthors(postIds: Collection<String>): List<String>
}
