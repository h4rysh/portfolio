package com.remindcare.alarm

import android.app.*
import android.content.*
import android.os.Build
import com.remindcare.data.ReminderEntity
import java.time.*

class ReminderScheduler(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    fun schedule(r: ReminderEntity) {
        if (!r.enabled) return
        val now=ZonedDateTime.now(); var time=now.withHour(r.hour).withMinute(r.minute).withSecond(0).withNano(0)
        if (!time.isAfter(now)) time=time.plusDays(1)
        val intent=Intent(context, ReminderReceiver::class.java).putExtra("reminder_id", r.id)
        val pi=PendingIntent.getBroadcast(context, r.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.toInstant().toEpochMilli(), pi)
    }
    fun cancel(id:Long) { manager.cancel(PendingIntent.getBroadcast(context,id.toInt(),Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)) }
    fun scheduleRetry(id:Long, attempt:Int) { val intent=Intent(context,ReminderReceiver::class.java).putExtra("reminder_id",id).putExtra("attempt",attempt); val pi=PendingIntent.getBroadcast(context,(id+100_000+attempt).toInt(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+300_000,pi) }
    fun cancelRetries(id:Long) { (1..3).forEach { attempt-> manager.cancel(PendingIntent.getBroadcast(context,(id+100_000+attempt).toInt(),Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)) } }
}
