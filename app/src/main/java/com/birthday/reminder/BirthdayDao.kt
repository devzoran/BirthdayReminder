package com.birthday.reminder

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BirthdayDao {
    
    @Query("SELECT * FROM birthdays ORDER BY sortOrder ASC, name ASC")
    fun getAllBirthdays(): LiveData<List<Birthday>>
    
    @Query("SELECT * FROM birthdays WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getEnabledBirthdays(): LiveData<List<Birthday>>
    
    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthdayById(id: Long): Birthday?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(birthday: Birthday): Long
    
    @Update
    suspend fun update(birthday: Birthday)
    
    @Delete
    suspend fun delete(birthday: Birthday)
    
    @Query("DELETE FROM birthdays WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE birthdays SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
    
    @Transaction
    suspend fun updateSortOrders(birthdays: List<Birthday>) {
        birthdays.forEachIndexed { index, birthday ->
            updateSortOrder(birthday.id, index)
        }
    }
    
    @Query("SELECT * FROM birthdays WHERE isEnabled = 1")
    suspend fun getEnabledBirthdaysSync(): List<Birthday>
}
