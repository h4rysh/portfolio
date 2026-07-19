package com.remindcare.alarm

import android.app.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindcare.data.*
import kotlinx.coroutines.*

class ReminderAlarmActivity: ComponentActivity() {
    private var tone:Ringtone?=null
    override fun onCreate(saved:Bundle?) { super.onCreate(saved); val id=intent.getLongExtra("reminder_id",0); window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); tone=RingtoneManager.getRingtone(this,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); tone?.play(); (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(longArrayOf(0,700,300),0))
        setContent { var r by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ReminderEntity?>(null) }; androidx.compose.runtime.LaunchedEffect(id) { r=withContext(Dispatchers.IO){RemindCareDatabase.get(this@ReminderAlarmActivity).reminders().get(id)} }; AlarmScreen(r, { complete(id) }, { snooze(id) }) }
    }
    private fun stop(){tone?.stop(); (getSystemService(VIBRATOR_SERVICE) as Vibrator).cancel()}
    private fun complete(id:Long){ ReminderScheduler(this).cancelRetries(id); CoroutineScope(Dispatchers.IO).launch { val db=RemindCareDatabase.get(this@ReminderAlarmActivity); val h=db.history().forOccurrence(id,System.currentTimeMillis()-86_400_000,System.currentTimeMillis()+1); if(h!=null)db.history().update(h.copy(status=CompletionStatus.COMPLETED,completedAt=System.currentTimeMillis())) }; stop(); finish() }
    private fun snooze(id:Long){ val i=android.content.Intent(this,ReminderReceiver::class.java).putExtra("reminder_id",id); val pi=PendingIntent.getBroadcast(this,id.toInt(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); getSystemService(AlarmManager::class.java).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+300_000,pi); stop(); finish() }
    override fun onDestroy(){stop();super.onDestroy()}
}
@Composable private fun AlarmScreen(r:ReminderEntity?, complete:()->Unit, snooze:()->Unit) { MaterialTheme { Surface(modifier=Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.errorContainer) { Column(Modifier.fillMaxSize().padding(28.dp),verticalArrangement=Arrangement.SpaceEvenly,horizontalAlignment=Alignment.CenterHorizontally) { Text("TIME TO ${if(r?.type==ReminderType.MEDICINE) "TAKE MEDICINE" else "REMEMBER"}",fontSize=27.sp); Text(r?.title ?: "Loading…",fontSize=38.sp); r?.quantity?.takeIf{it.isNotBlank()}?.let{Text("Take: $it",fontSize=24.sp)}; r?.location?.takeIf{it.isNotBlank()}?.let{Text("Location: $it",fontSize=22.sp)}; Button(onClick=complete,modifier=Modifier.fillMaxWidth().height(88.dp)){Text("COMPLETE",fontSize=25.sp)}; OutlinedButton(onClick=snooze,modifier=Modifier.fillMaxWidth().height(72.dp)){Text("SNOOZE 5 MINUTES",fontSize=21.sp)} } } } }
