package com.hasibzero.bakikhata.data.di

import android.content.Context
import androidx.room.Room
import com.hasibzero.bakikhata.data.db.AppDatabase
import com.hasibzero.bakikhata.data.db.dao.CreditEntryDao
import com.hasibzero.bakikhata.data.db.dao.CustomerDao
import com.hasibzero.bakikhata.data.db.dao.PaymentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bakikhata_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }
    
    @Provides
    @Singleton
    fun provideCreditEntryDao(database: AppDatabase): CreditEntryDao {
        return database.creditEntryDao()
    }
    
    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }
}
