package com.remindcare.data

import kotlinx.coroutines.flow.Flow
import java.time.*

class ReminderRepository(private val db: RemindCareDatabase) {
    val profile = db.profiles().observe(); val reminders = db.reminders().observeAll(); val history = db.history().observeAll()
    fun todayHistory(): Flow<List<HistoryEntity>> { val z=ZoneId.systemDefault(); val start=LocalDate.now().atStartOfDay(z).toInstant().toEpochMilli(); return db.history().observeRange(start, start+86_400_000) }
    suspend fun saveProfile(role: UserRole, name: String) = db.profiles().save(ProfileEntity(role=role, displayName=name, pairingCode=(100000..999999).random().toString()))
    suspend fun add(r: ReminderEntity) = db.reminders().insert(r)
    suspend fun update(r: ReminderEntity) = db.reminders().update(r)
    suspend fun delete(r: ReminderEntity) = db.reminders().delete(r)
    suspend fun get(id:Long)=db.reminders().get(id)
    suspend fun recordDue(r: ReminderEntity, at: Long): HistoryEntity { val old=db.history().forOccurrence(r.id, at-60_000, at+60_000); return old ?: HistoryEntity(reminderId=r.id,title=r.title,scheduledAt=at).also { db.history().insert(it) } }
    suspend fun status(id:Long, status: CompletionStatus, photo:String?=null) { val h=historyForId(id) ?: return; db.history().update(h.copy(status=status, completedAt=System.currentTimeMillis(), photoUri=photo, attempts=h.attempts+1)) }
    private suspend fun historyForId(id:Long):HistoryEntity? { val now=System.currentTimeMillis(); return db.history().forOccurrence(id, now-24*60*60_000, now+60*60_000) }
}
