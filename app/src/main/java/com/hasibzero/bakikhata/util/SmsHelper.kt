package com.hasibzero.bakikhata.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hasibzero.bakikhata.R

object SmsHelper {
    
    fun sendSmsReminder(
        context: Context,
        phoneNumber: String,
        customerName: String,
        dueAmount: Double,
        businessName: String = context.getString(R.string.app_name)
    ) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = context.getString(
            R.string.sms_template,
            customerName,
            dueAmount,
            businessName
        )
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show()
        }
    }
    
    fun formatPhoneNumber(phoneNumber: String): String {
        return phoneNumber.trim().replace(Regex("[^0-9+]"), "")
    }
    
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val cleaned = formatPhoneNumber(phoneNumber)
        return cleaned.length >= 11 && cleaned.length <= 15
    }
}
