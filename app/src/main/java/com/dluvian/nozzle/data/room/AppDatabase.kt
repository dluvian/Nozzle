package com.dluvian.nozzle.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.HashtagDao
import com.dluvian.nozzle.data.room.dao.MentionDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.dao.RepostDao
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.room.entity.EventRelayEntity
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.entity.ReactionEntity
import com.dluvian.nozzle.data.room.entity.RepostEntity

@Database(
    version = 25,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
    ],
    entities = [
        ContactEntity::class,
        EventRelayEntity::class,
        Nip65Entity::class,
        PostEntity::class,
        ProfileEntity::class,
        ReactionEntity::class,
        HashtagEntity::class,
        MentionEntity::class,
        AccountEntity::class,
        RepostEntity::class,
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun eventRelayDao(): EventRelayDao
    abstract fun nip65Dao(): Nip65Dao
    abstract fun profileDao(): ProfileDao
    abstract fun postDao(): PostDao
    abstract fun reactionDao(): ReactionDao
    abstract fun hashtagDao(): HashtagDao
    abstract fun mentionDao(): MentionDao
    abstract fun accountDao(): AccountDao
    abstract fun repostDao(): RepostDao
}
