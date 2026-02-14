package com.hasibzero.bakikhata.data.db.dao

import androidx.room.*
import com.hasibzero.bakikhata.data.db.entity.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers WHERE id = :customerId AND isDeleted = 0")
    fun getCustomerById(customerId: Long): Flow<Customer?>
    
    @Query("SELECT * FROM customers WHERE (name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<Customer>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long
    
    @Update
    suspend fun updateCustomer(customer: Customer)
    
    @Query("UPDATE customers SET isDeleted = 1, deletedAt = :timestamp WHERE id = :customerId")
    suspend fun softDeleteCustomer(customerId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    fun getCustomerCount(): Flow<Int>
}
