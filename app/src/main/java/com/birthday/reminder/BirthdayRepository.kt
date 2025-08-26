package com.birthday.reminder

import androidx.lifecycle.LiveData

class BirthdayRepository(private val birthdayDao: BirthdayDao) {
    
    val allBirthdays: LiveData<List<Birthday>> = birthdayDao.getAllBirthdays()
    val enabledBirthdays: LiveData<List<Birthday>> = birthdayDao.getEnabledBirthdays()
    
    suspend fun insert(birthday: Birthday): Long {
        return birthdayDao.insert(birthday)
    }
    
    suspend fun update(birthday: Birthday) {
        birthdayDao.update(birthday)
    }
    
    suspend fun delete(birthday: Birthday) {
        birthdayDao.delete(birthday)
    }
    
    suspend fun getBirthdayById(id: Long): Birthday? {
        return birthdayDao.getBirthdayById(id)
    }
    
    suspend fun getEnabledBirthdaysSync(): List<Birthday> {
        return birthdayDao.getEnabledBirthdaysSync()
    }
    
    suspend fun updateSortOrders(birthdays: List<Birthday>) {
        birthdayDao.updateSortOrders(birthdays)
    }
}
