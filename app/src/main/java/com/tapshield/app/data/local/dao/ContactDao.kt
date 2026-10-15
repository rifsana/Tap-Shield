package com.tapshield.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tapshield.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    suspend fun getAllContactsSync(): List<ContactEntity>

    @Query("SELECT * FROM trusted_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT COUNT(*) FROM trusted_contacts")
    fun getContactCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trusted_contacts")
    suspend fun getContactCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("UPDATE trusted_contacts SET isPrimary = 0")
    suspend fun clearPrimaryFlags()

    @Query("UPDATE trusted_contacts SET isPrimary = 1 WHERE id = :id")
    suspend fun setPrimaryFlag(id: Long)

    @Transaction
    suspend fun setSinglePrimary(id: Long) {
        clearPrimaryFlags()
        setPrimaryFlag(id)
    }
}
