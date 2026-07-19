package com.remindcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.DayOfWeek

enum class UserRole { PATIENT, CAREGIVER }
enum class ReminderType { MEDICINE, MEAL, WATER, EXERCISE, APPOINTMENT, SLEEP, GENERAL, CUSTOM }
enum class CompletionStatus { PENDING, COMPLETED, MISSED, SKIPPED, SNOOZED }

@Entity(tableName = "profiles")
data class ProfileEntity(@PrimaryKey val id: String = "local", val role: UserRole, val displayName: String, val pairingCode: String = "")

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: ReminderType,
    val hour: Int,
    val minute: Int,
    val repeatDays: Set<Int> = emptySet(),
    val imageUri: String? = null,
    val quantity: String = "",
    val location: String = "",
    val notes: String = "",
    val requiresPhoto: Boolean = false,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val title: String,
    val scheduledAt: Long,
    val completedAt: Long? = null,
    val status: CompletionStatus = CompletionStatus.PENDING,
    val photoUri: String? = null,
    val attempts: Int = 0
)

class Converters {
    @TypeConverter fun fromRole(v: UserRole) = v.name
    @TypeConverter fun toRole(v: String) = UserRole.valueOf(v)
    @TypeConverter fun fromType(v: ReminderType) = v.name
    @TypeConverter fun toType(v: String) = ReminderType.valueOf(v)
    @TypeConverter fun fromStatus(v: CompletionStatus) = v.name
    @TypeConverter fun toStatus(v: String) = CompletionStatus.valueOf(v)
    @TypeConverter fun fromSet(v: Set<Int>) = v.joinToString(",")
    @TypeConverter fun toSet(v: String) = if (v.isBlank()) emptySet() else v.split(",").map { it.toInt() }.toSet()
}
