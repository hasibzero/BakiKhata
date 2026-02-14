package com.hasibzero.bakikhata.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hasibzero.bakikhata.data.db.dao.CreditEntryDao
import com.hasibzero.bakikhata.data.db.dao.CustomerDao
import com.hasibzero.bakikhata.data.db.dao.PaymentDao
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.db.entity.Customer
import com.hasibzero.bakikhata.data.db.entity.Payment

@Database(
    entities = [
        Customer::class,
        CreditEntry::class,
        Payment::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun creditEntryDao(): CreditEntryDao
    abstract fun paymentDao(): PaymentDao
}
