package com.hasibzero.bakikhata.util

import android.content.Context
import android.os.Environment
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.db.entity.Customer
import com.hasibzero.bakikhata.data.db.entity.Payment
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {
    
    fun exportCustomerReport(
        context: Context,
        customer: Customer,
        credits: List<CreditEntry>,
        payments: List<Payment>,
        totalCredit: Double,
        totalPayment: Double,
        fileName: String = "customer_report_${customer.id}_${System.currentTimeMillis()}.xlsx"
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
            val workbook = XSSFWorkbook()
            
            // Create header style
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    bold = true
                }
                setFont(font)
            }
            
            // Customer Details Sheet
            val detailsSheet = workbook.createSheet("Customer Details")
            var rowNum = 0
            
            val nameRow = detailsSheet.createRow(rowNum++)
            nameRow.createCell(0).setCellValue("Name:")
            nameRow.createCell(1).setCellValue(customer.name)
            
            customer.phoneNumber?.let {
                val phoneRow = detailsSheet.createRow(rowNum++)
                phoneRow.createCell(0).setCellValue("Phone:")
                phoneRow.createCell(1).setCellValue(it)
            }
            
            customer.address?.let {
                val addressRow = detailsSheet.createRow(rowNum++)
                addressRow.createCell(0).setCellValue("Address:")
                addressRow.createCell(1).setCellValue(it)
            }
            
            rowNum++ // Empty row
            
            val summaryRow1 = detailsSheet.createRow(rowNum++)
            summaryRow1.createCell(0).setCellValue("Total Credit:")
            summaryRow1.createCell(1).setCellValue("৳$totalCredit")
            
            val summaryRow2 = detailsSheet.createRow(rowNum++)
            summaryRow2.createCell(0).setCellValue("Total Payment:")
            summaryRow2.createCell(1).setCellValue("৳$totalPayment")
            
            val summaryRow3 = detailsSheet.createRow(rowNum++)
            summaryRow3.createCell(0).setCellValue("Balance Due:")
            summaryRow3.createCell(1).setCellValue("৳${totalCredit - totalPayment}")
            
            // Credit Entries Sheet
            if (credits.isNotEmpty()) {
                val creditsSheet = workbook.createSheet("Credit Entries")
                rowNum = 0
                
                val headerRow = creditsSheet.createRow(rowNum++)
                val headers = listOf("Product", "Quantity", "Price", "Total", "Date")
                headers.forEachIndexed { index, header ->
                    headerRow.createCell(index).apply {
                        setCellValue(header)
                        cellStyle = headerStyle
                    }
                }
                
                credits.forEach { credit ->
                    val row = creditsSheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(credit.productName)
                    row.createCell(1).setCellValue(credit.quantity)
                    row.createCell(2).setCellValue(credit.price)
                    row.createCell(3).setCellValue(credit.totalAmount)
                    row.createCell(4).setCellValue(DateUtils.formatDate(credit.date))
                }
                
                // Auto-size columns
                for (i in 0..4) {
                    creditsSheet.autoSizeColumn(i)
                }
            }
            
            // Payments Sheet
            if (payments.isNotEmpty()) {
                val paymentsSheet = workbook.createSheet("Payments")
                rowNum = 0
                
                val headerRow = paymentsSheet.createRow(rowNum++)
                val headers = listOf("Date", "Amount", "Notes")
                headers.forEachIndexed { index, header ->
                    headerRow.createCell(index).apply {
                        setCellValue(header)
                        cellStyle = headerStyle
                    }
                }
                
                payments.forEach { payment ->
                    val row = paymentsSheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(DateUtils.formatDate(payment.date))
                    row.createCell(1).setCellValue(payment.amount)
                    row.createCell(2).setCellValue(payment.notes ?: "")
                }
                
                // Auto-size columns
                for (i in 0..2) {
                    paymentsSheet.autoSizeColumn(i)
                }
            }
            
            // Write to file
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportAllCustomers(
        context: Context,
        customers: List<Customer>,
        creditsMap: Map<Long, Double>,
        paymentsMap: Map<Long, Double>,
        fileName: String = "all_customers_${System.currentTimeMillis()}.xlsx"
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
            val workbook = XSSFWorkbook()
            
            // Create header style
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    bold = true
                }
                setFont(font)
            }
            
            val sheet = workbook.createSheet("All Customers")
            var rowNum = 0
            
            val headerRow = sheet.createRow(rowNum++)
            val headers = listOf("Name", "Phone", "Address", "Total Credit", "Total Payment", "Balance Due")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }
            
            customers.forEach { customer ->
                val row = sheet.createRow(rowNum++)
                val credit = creditsMap[customer.id] ?: 0.0
                val payment = paymentsMap[customer.id] ?: 0.0
                
                row.createCell(0).setCellValue(customer.name)
                row.createCell(1).setCellValue(customer.phoneNumber ?: "")
                row.createCell(2).setCellValue(customer.address ?: "")
                row.createCell(3).setCellValue(credit)
                row.createCell(4).setCellValue(payment)
                row.createCell(5).setCellValue(credit - payment)
            }
            
            // Auto-size columns
            for (i in 0..5) {
                sheet.autoSizeColumn(i)
            }
            
            // Write to file
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
