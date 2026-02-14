package com.hasibzero.bakikhata.data.repository

import com.hasibzero.bakikhata.data.db.dao.PaymentDao
import com.hasibzero.bakikhata.data.db.entity.Payment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByCustomer(customerId)
    
    fun getPaymentById(paymentId: Long): Flow<Payment?> =
        paymentDao.getPaymentById(paymentId)
    
    fun getTotalPaymentByCustomer(customerId: Long): Flow<Double?> =
        paymentDao.getTotalPaymentByCustomer(customerId)
    
    fun getTotalPaymentAmount(): Flow<Double?> =
        paymentDao.getTotalPaymentAmount()
    
    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment)
    
    suspend fun updatePayment(payment: Payment) =
        paymentDao.updatePayment(payment)
    
    suspend fun deletePayment(paymentId: Long) =
        paymentDao.softDeletePayment(paymentId)
    
    fun getRecentPayments(limit: Int): Flow<List<Payment>> =
        paymentDao.getRecentPayments(limit)
}
