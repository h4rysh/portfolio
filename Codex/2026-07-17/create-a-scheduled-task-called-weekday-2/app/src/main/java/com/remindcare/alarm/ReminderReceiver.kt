package com.remindcare.alarm

import android.content.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.remindcare.MainActivity
import com.remindcare.data.RemindCareDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { val pending=goAsync(); val id=intent.getLongExtra("reminder_id",0); val attempt=intent.getIntExtra("attempt",0)
        CoroutineScope(Dispatchers.IO).launch { val db=RemindCareDatabase.get(context); val r=db.reminders().get(id)
            if(r!=null) { val now=System.currentTimeMillis(); val prior=db.history().forOccurrence(id,now-86_400_000,now+1); if(prior==null) db.history().insert(com.remindcare.data.HistoryEntity(reminderId=id,title=r.title,scheduledAt=now)) else if(attempt>=3) db.history().update(prior.copy(status=com.remindcare.data.CompletionStatus.MISSED,attempts=attempt)) else db.history().update(prior.copy(attempts=attempt))
                NotificationChannels.ensure(context)
                val alarmIntent=Intent(context, ReminderAlarmActivity::class.java).putExtra("reminder_id",id)
                val full=android.app.PendingIntent.getActivity(context,id.toInt(),alarmIntent,android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                val note=NotificationCompat.Builder(context,NotificationChannels.ALARM).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Time for ${r.title}").setContentText("Tap to complete this reminder").setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).setFullScreenIntent(full,true).setContentIntent(full).build()
                NotificationManagerCompat.from(context).notify(id.toInt(),note)
                if(attempt<3) ReminderScheduler(context).scheduleRetry(id,attempt+1)
                if(attempt==0) ReminderScheduler(context).schedule(r)
            }; pending.finish()
        }
    }
}
object NotificationChannels { const val ALARM="reminder_alarm"; fun ensure(context:Context) { val manager=context.getSystemService(android.app.NotificationManager::class.java); manager.createNotificationChannel(android.app.NotificationChannel(ALARM,"Reminder alarms",android.app.NotificationManager.IMPORTANCE_HIGH).apply { description="Loud, persistent care reminders"; enableVibration(true); setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM), android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_ALARM).build()) }) } }
class BootReceiver: BroadcastReceiver() { override fun onReceive(context:Context,intent:Intent) { val pending=goAsync(); CoroutineScope(Dispatchers.IO).launch { val reminders=RemindCareDatabase.get(context).reminders().observeAll().first(); reminders.forEach{ReminderScheduler(context).schedule(it)}; pending.finish() } } }
