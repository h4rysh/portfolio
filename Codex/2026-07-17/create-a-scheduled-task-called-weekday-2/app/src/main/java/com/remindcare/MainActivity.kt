package com.remindcare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.remindcare.alarm.*
import com.remindcare.data.*
import com.remindcare.ui.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) registerForActivityResult(ActivityResultContracts.RequestPermission()){}.launch(Manifest.permission.POST_NOTIFICATIONS)
        val db=RemindCareDatabase.get(this); val vm=ViewModelProvider(this, CareViewModel.Factory(ReminderRepository(db),ReminderScheduler(this)))[CareViewModel::class.java]
        setContent { val profile by vm.profile.collectAsStateWithLifecycle(null); RemindCareTheme { Surface { if(profile==null) RoleScreen(vm) else HomeScreen(vm,profile!!) } } }
    }
}
class CareViewModel(private val repo:ReminderRepository,private val scheduler:ReminderScheduler):ViewModel(){
    val profile=repo.profile.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),null)
    val reminders=repo.reminders.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val history=repo.history.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    fun choose(role:UserRole,name:String){viewModelScope.launch{repo.saveProfile(role,name); if(role==UserRole.PATIENT && reminders.value.isEmpty()) sampleData()}}
    fun save(r:ReminderEntity){viewModelScope.launch{val id=if(r.id==0)repo.add(r) else {repo.update(r);r.id}; scheduler.schedule(r.copy(id=id))}}
    fun remove(r:ReminderEntity){viewModelScope.launch{scheduler.cancel(r.id);repo.delete(r)}}
    fun complete(id:Long, photo:String?=null){viewModelScope.launch{repo.get(id)?.let{repo.recordDue(it,System.currentTimeMillis())};repo.status(id,CompletionStatus.COMPLETED,photo)}}
    private suspend fun sampleData(){ listOf(ReminderEntity(title="Morning medicine",type=ReminderType.MEDICINE,hour=9,minute=0,quantity="2 tablets",location="Kitchen cabinet"),ReminderEntity(title="Drink water",type=ReminderType.WATER,hour=11,minute=0),ReminderEntity(title="Afternoon walk",type=ReminderType.EXERCISE,hour=17,minute=0)).forEach{save(it)} }
    class Factory(private val r:ReminderRepository,private val s:ReminderScheduler):ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(c:Class<T>)=CareViewModel(r,s) as T }
}
