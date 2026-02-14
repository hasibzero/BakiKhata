package com.hasibzero.bakikhata.util

import android.content.Context
import com.hasibzero.bakikhata.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
    
    private const val BACKUP_FILE_NAME = "bakikhata_backup.zip"
    
    suspend fun createBackup(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            // Close database connections
            val dbPath = context.getDatabasePath("bakikhata_database")
            if (!dbPath.exists()) {
                return@withContext null
            }
            
            // Create backup directory
            val backupDir = File(context.getExternalFilesDir(null), "Backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.zip")
            val zipOutputStream = ZipOutputStream(FileOutputStream(backupFile))
            
            // Add database file to zip
            val dbFile = context.getDatabasePath("bakikhata_database")
            if (dbFile.exists()) {
                zipOutputStream.putNextEntry(ZipEntry(dbFile.name))
                FileInputStream(dbFile).use { input ->
                    input.copyTo(zipOutputStream)
                }
                zipOutputStream.closeEntry()
            }
            
            // Add shared preferences
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists()) {
                prefsDir.listFiles()?.forEach { file ->
                    zipOutputStream.putNextEntry(ZipEntry("shared_prefs/${file.name}"))
                    FileInputStream(file).use { input ->
                        input.copyTo(zipOutputStream)
                    }
                    zipOutputStream.closeEntry()
                }
            }
            
            zipOutputStream.close()
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun restoreBackup(context: Context, backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext false
            }
            
            val zipInputStream = ZipInputStream(FileInputStream(backupFile))
            var entry: ZipEntry? = zipInputStream.nextEntry
            
            while (entry != null) {
                val entryName = entry.name
                
                when {
                    entryName.startsWith("shared_prefs/") -> {
                        val prefsFile = File(context.applicationInfo.dataDir, entryName)
                        prefsFile.parentFile?.mkdirs()
                        FileOutputStream(prefsFile).use { output ->
                            zipInputStream.copyTo(output)
                        }
                    }
                    entryName == "bakikhata_database" -> {
                        val dbFile = context.getDatabasePath(entryName)
                        dbFile.parentFile?.mkdirs()
                        FileOutputStream(dbFile).use { output ->
                            zipInputStream.copyTo(output)
                        }
                    }
                }
                
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
            
            zipInputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getBackupFiles(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "Backups")
        if (!backupDir.exists()) {
            return emptyList()
        }
        return backupDir.listFiles()?.filter { it.extension == "zip" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
