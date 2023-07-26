package com.dluvian.nozzle.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dluvian.nozzle.data.room.dao.*
import com.dluvian.nozzle.data.room.entity.*

@Database(
    entities = [
        ContactEntity::class,
        EventRelayEntity::class,
        Nip65Entity::class,
        PostEntity::class,
        ProfileEntity::class,
        ReactionEntity::class,
    ],
    version = 8
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun eventRelayDao(): EventRelayDao
    abstract fun nip65Dao(): Nip65Dao
    abstract fun profileDao(): ProfileDao
    abstract fun postDao(): PostDao
    abstract fun reactionDao(): ReactionDao
}
