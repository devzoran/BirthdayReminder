package com.birthday.reminder

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class BirthdayViewModel(private val repository: BirthdayRepository) : ViewModel() {
    
    val allBirthdays: LiveData<List<Birthday>> = repository.allBirthdays
    val enabledBirthdays: LiveData<List<Birthday>> = repository.enabledBirthdays
    
    fun insert(birthday: Birthday) = viewModelScope.launch {
        val id = repository.insert(birthday)
        // 插入成功后设置提醒
        if (birthday.isEnabled) {
            BirthdayReminderManager.scheduleReminder(birthday.copy(id = id))
        }
    }
    
    fun update(birthday: Birthday) = viewModelScope.launch {
        repository.update(birthday)
        // 更新提醒
        BirthdayReminderManager.cancelReminder(birthday)
        if (birthday.isEnabled) {
            BirthdayReminderManager.scheduleReminder(birthday)
        }
    }
    
    fun delete(birthday: Birthday) = viewModelScope.launch {
        repository.delete(birthday)
        // 取消提醒
        BirthdayReminderManager.cancelReminder(birthday)
    }
    
    suspend fun getBirthdayById(id: Long): Birthday? {
        return repository.getBirthdayById(id)
    }
    
    fun updateSortOrders(birthdays: List<Birthday>) = viewModelScope.launch {
        repository.updateSortOrders(birthdays)
    }
}

class BirthdayViewModelFactory(private val repository: BirthdayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BirthdayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BirthdayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
