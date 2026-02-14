package com.hasibzero.bakikhata.util

import android.content.Context
import android.os.Environment
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.db.entity.Customer
import com.hasibzero.bakikhata.data.db.entity.Payment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    
    fun exportCustomerReport(
        context: Context,
        customer: Customer,
        credits: List<CreditEntry>,
        payments: List<Payment>,
        totalCredit: Double,
        totalPayment: Double,
        fileName: String = "customer_report_${customer.id}_${System.currentTimeMillis()}.pdf"
    ): File? {
        return try {
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "BakiKhata/Reports"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, fileName)
            val writer = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)
            
            // Title
            document.add(
                Paragraph("Customer Report")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(Paragraph(" "))
            
            // Customer Details
            document.add(Paragraph("Customer Details").setFontSize(16f).setBold())
            document.add(Paragraph("Name: ${customer.name}"))
            customer.phoneNumber?.let {
                document.add(Paragraph("Phone: $it"))
            }
            customer.address?.let {
                document.add(Paragraph("Address: $it"))
            }
            
            document.add(Paragraph(" "))
            
            // Summary
            document.add(Paragraph("Summary").setFontSize(16f).setBold())
            document.add(Paragraph("Total Credit: ৳$totalCredit"))
            document.add(Paragraph("Total Payment: ৳$totalPayment"))
            document.add(Paragraph("Balance Due: ৳${totalCredit - totalPayment}"))
            
            document.add(Paragraph(" "))
            
            // Credit Entries Table
            if (credits.isNotEmpty()) {
                document.add(Paragraph("Credit Entries").setFontSize(16f).setBold())
                val creditTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 2f, 2f)))
                    .useAllAvailableWidth()
                
                creditTable.addCell("Product")
                creditTable.addCell("Quantity")
                creditTable.addCell("Price")
                creditTable.addCell("Total")
                
                credits.forEach { credit ->
                    creditTable.addCell(credit.productName)
                    creditTable.addCell(credit.quantity.toString())
                    creditTable.addCell("৳${credit.price}")
                    creditTable.addCell("৳${credit.totalAmount}")
                }
                
                document.add(creditTable)
                document.add(Paragraph(" "))
            }
            
            // Payments Table
            if (payments.isNotEmpty()) {
                document.add(Paragraph("Payments").setFontSize(16f).setBold())
                val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f)))
                    .useAllAvailableWidth()
                
                paymentTable.addCell("Date")
                paymentTable.addCell("Amount")
                
                payments.forEach { payment ->
                    paymentTable.addCell(DateUtils.formatDate(payment.date))
                    paymentTable.addCell("৳${payment.amount}")
                }
                
                document.add(paymentTable)
            }
            
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportMonthlyReport(
        context: Context,
        customers: List<Customer>,
        totalCredit: Double,
        totalPayment: Double,
        month: Int,
        year: Int,
        fileName: String = "monthly_report_${month}_${year}.pdf"
    ): File? {
        return try {
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "BakiKhata/Reports"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, fileName)
            val writer = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)
            
            // Title
            document.add(
                Paragraph("Monthly Report - $month/$year")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(Paragraph(" "))
            
            // Summary
            document.add(Paragraph("Summary").setFontSize(16f).setBold())
            document.add(Paragraph("Total Customers: ${customers.size}"))
            document.add(Paragraph("Total Credit: ৳$totalCredit"))
            document.add(Paragraph("Total Payment: ৳$totalPayment"))
            document.add(Paragraph("Balance Due: ৳${totalCredit - totalPayment}"))
            
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
