package com.hasibzero.bakikhata.data.db.dao

import androidx.room.*
import com.hasibzero.bakikhata.data.db.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    
    @Query("SELECT * FROM payments WHERE customerId = :customerId AND isDeleted = 0 ORDER BY date DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>>
    
    @Query("SELECT * FROM payments WHERE id = :paymentId AND isDeleted = 0")
    fun getPaymentById(paymentId: Long): Flow<Payment?>
    
    @Query("SELECT SUM(amount) FROM payments WHERE customerId = :customerId AND isDeleted = 0")
    fun getTotalPaymentByCustomer(customerId: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM payments WHERE isDeleted = 0")
    fun getTotalPaymentAmount(): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long
    
    @Update
    suspend fun updatePayment(payment: Payment)
    
    @Query("UPDATE payments SET isDeleted = 1, deletedAt = :timestamp WHERE id = :paymentId")
    suspend fun softDeletePayment(paymentId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM payments WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecentPayments(limit: Int): Flow<List<Payment>>
}
