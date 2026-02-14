package com.hasibzero.bakikhata.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hasibzero.bakikhata.MainActivity
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import com.hasibzero.bakikhata.data.repository.CustomerRepository
import com.hasibzero.bakikhata.data.repository.PaymentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val customerRepository: CustomerRepository,
    private val creditEntryRepository: CreditEntryRepository,
    private val paymentRepository: PaymentRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "due_reminders"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            val customers = customerRepository.getAllCustomers().first()
            var customersWithDue = 0
            
            customers.forEach { customer ->
                val totalCredit = creditEntryRepository.getTotalCreditByCustomer(customer.id).first() ?: 0.0
                val totalPayment = paymentRepository.getTotalPaymentByCustomer(customer.id).first() ?: 0.0
                val due = totalCredit - totalPayment
                
                if (due > 0) {
                    customersWithDue++
                }
            }
            
            if (customersWithDue > 0) {
                showNotification(customersWithDue)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification(count: Int) {
        createNotificationChannel()
        
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText("$count ${applicationContext.getString(R.string.notification_message)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notification_channel_name)
            val descriptionText = applicationContext.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
