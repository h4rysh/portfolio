package com.remindcare.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface ProfileDao { @Query("SELECT * FROM profiles WHERE id='local'") fun observe(): Flow<ProfileEntity?>; @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(profile: ProfileEntity) }
@Dao interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY hour, minute") fun observeAll(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id=:id") suspend fun get(id: Long): ReminderEntity?
    @Insert suspend fun insert(r: ReminderEntity): Long
    @Update suspend fun update(r: ReminderEntity)
    @Delete suspend fun delete(r: ReminderEntity)
}
@Dao interface HistoryDao {
    @Query("SELECT * FROM history WHERE scheduledAt BETWEEN :start AND :end ORDER BY scheduledAt") fun observeRange(start: Long, end: Long): Flow<List<HistoryEntity>>
    @Query("SELECT * FROM history ORDER BY scheduledAt DESC") fun observeAll(): Flow<List<HistoryEntity>>
    @Query("SELECT * FROM history WHERE reminderId=:reminderId AND scheduledAt BETWEEN :start AND :end LIMIT 1") suspend fun forOccurrence(reminderId: Long, start: Long, end: Long): HistoryEntity?
    @Insert suspend fun insert(h: HistoryEntity): Long
    @Update suspend fun update(h: HistoryEntity)
}
@Database(entities=[ProfileEntity::class, ReminderEntity::class, HistoryEntity::class], version=1, exportSchema=false)
@TypeConverters(Converters::class)
abstract class RemindCareDatabase : RoomDatabase() {
    abstract fun profiles(): ProfileDao; abstract fun reminders(): ReminderDao; abstract fun history(): HistoryDao
    companion object { @Volatile private var instance: RemindCareDatabase? = null
        fun get(context: Context) = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(context.applicationContext, RemindCareDatabase::class.java, "remindcare.db").build().also { instance=it } }
    }
}
