package com.hasibzero.bakikhata.data.db.dao

import androidx.room.*
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditEntryDao {
    
    @Query("SELECT * FROM credit_entries WHERE customerId = :customerId AND isDeleted = 0 ORDER BY date DESC")
    fun getCreditEntriesByCustomer(customerId: Long): Flow<List<CreditEntry>>
    
    @Query("SELECT * FROM credit_entries WHERE id = :entryId AND isDeleted = 0")
    fun getCreditEntryById(entryId: Long): Flow<CreditEntry?>
    
    @Query("SELECT SUM(totalAmount) FROM credit_entries WHERE customerId = :customerId AND isDeleted = 0")
    fun getTotalCreditByCustomer(customerId: Long): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM credit_entries WHERE isDeleted = 0")
    fun getTotalCreditAmount(): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM credit_entries WHERE date >= :startOfDay AND date < :endOfDay AND isDeleted = 0")
    fun getTodaysCreditAmount(startOfDay: Long, endOfDay: Long): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditEntry(creditEntry: CreditEntry): Long
    
    @Update
    suspend fun updateCreditEntry(creditEntry: CreditEntry)
    
    @Query("UPDATE credit_entries SET isDeleted = 1, deletedAt = :timestamp WHERE id = :entryId")
    suspend fun softDeleteCreditEntry(entryId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM credit_entries WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecentCreditEntries(limit: Int): Flow<List<CreditEntry>>
}
