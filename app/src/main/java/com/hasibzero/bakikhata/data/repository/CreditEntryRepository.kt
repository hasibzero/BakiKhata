package com.hasibzero.bakikhata.data.repository

import com.hasibzero.bakikhata.data.db.dao.CreditEntryDao
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditEntryRepository @Inject constructor(
    private val creditEntryDao: CreditEntryDao
) {
    fun getCreditEntriesByCustomer(customerId: Long): Flow<List<CreditEntry>> =
        creditEntryDao.getCreditEntriesByCustomer(customerId)
    
    fun getCreditEntryById(entryId: Long): Flow<CreditEntry?> =
        creditEntryDao.getCreditEntryById(entryId)
    
    fun getTotalCreditByCustomer(customerId: Long): Flow<Double?> =
        creditEntryDao.getTotalCreditByCustomer(customerId)
    
    fun getTotalCreditAmount(): Flow<Double?> =
        creditEntryDao.getTotalCreditAmount()
    
    fun getTodaysCreditAmount(startOfDay: Long, endOfDay: Long): Flow<Double?> =
        creditEntryDao.getTodaysCreditAmount(startOfDay, endOfDay)
    
    suspend fun insertCreditEntry(creditEntry: CreditEntry): Long =
        creditEntryDao.insertCreditEntry(creditEntry)
    
    suspend fun updateCreditEntry(creditEntry: CreditEntry) =
        creditEntryDao.updateCreditEntry(creditEntry)
    
    suspend fun deleteCreditEntry(entryId: Long) =
        creditEntryDao.softDeleteCreditEntry(entryId)
    
    fun getRecentCreditEntries(limit: Int): Flow<List<CreditEntry>> =
        creditEntryDao.getRecentCreditEntries(limit)
}
