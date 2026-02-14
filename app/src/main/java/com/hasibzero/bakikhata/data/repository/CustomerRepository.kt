package com.hasibzero.bakikhata.data.repository

import com.hasibzero.bakikhata.data.db.dao.CustomerDao
import com.hasibzero.bakikhata.data.db.entity.Customer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    
    fun getCustomerById(customerId: Long): Flow<Customer?> = customerDao.getCustomerById(customerId)
    
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customerId: Long) = customerDao.softDeleteCustomer(customerId)
    
    fun getCustomerCount(): Flow<Int> = customerDao.getCustomerCount()
}
