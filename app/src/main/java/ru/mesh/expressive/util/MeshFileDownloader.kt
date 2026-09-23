package ru.mesh.expressive.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.mesh.expressive.data.local.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class DownloadResult {
    data class Success(val uri: Uri, val fileName: String, val mimeType: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

object MeshFileDownloader {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }

    suspend fun downloadFile(
        context: Context,
        url: String,
        suggestedFileName: String,
        sessionManager: SessionManager? = null
    ): DownloadResult = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            return@withContext DownloadResult.Error("Ссылка на файл пуста")
        }

        try {
            val fullUrl = when {
                url.startsWith("http://") || url.startsWith("https://") -> url
                url.startsWith("/") -> "https://school.mos.ru$url"
                else -> "https://school.mos.ru/$url"
            }

            val requestBuilder = Request.Builder().url(fullUrl)

            // Если запрос к экосистеме mos.ru, подставим авторизационные токены
            if (fullUrl.contains("mos.ru") && sessionManager != null) {
                val token = sessionManager.authToken
                if (!token.isNullOrBlank()) {
                    val bearer = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", bearer)
                }
                val profId = sessionManager.profileId
                if (profId.isNotBlank()) {
                    requestBuilder.addHeader("Profile-Id", profId)
                }
                requestBuilder.addHeader("x-mes-subsystem", "familymp")
                requestBuilder.addHeader("client-type", "diary-mobile")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext DownloadResult.Error("Сервер вернул ошибку: ${response.code}")
            }

            val responseBody = response.body ?: return@withContext DownloadResult.Error("Тело ответа пустое")

            // Извлечение имени файла из Content-Disposition или URL
            var resolvedName = suggestedFileName.trim()
            val disposition = response.header("Content-Disposition")
            if (disposition != null) {
                val utf8Match = Regex("filename\\*=UTF-8''([^;]+)", RegexOption.IGNORE_CASE).find(disposition)
                if (utf8Match != null) {
                    resolvedName = java.net.URLDecoder.decode(utf8Match.groupValues[1], "UTF-8")
                } else {
                    val plainMatch = Regex("filename=\"?([^\";]+)\"?", RegexOption.IGNORE_CASE).find(disposition)
                    if (plainMatch != null) {
                        resolvedName = plainMatch.groupValues[1]
                    }
                }
            }

            if (resolvedName.isBlank() || resolvedName == "Файл") {
                val pathSegment = Uri.parse(fullUrl).lastPathSegment
                if (!pathSegment.isNullOrBlank()) {
                    resolvedName = pathSegment
                }
            }
            if (resolvedName.isBlank()) {
                resolvedName = "mesh_file_${System.currentTimeMillis()}"
            }

            val mimeType = getMimeType(resolvedName)

            // Сохранение файла
            val savedUri = saveToDownloads(context, responseBody.byteStream(), resolvedName, mimeType)
                ?: return@withContext DownloadResult.Error("Не удалось сохранить файл на устройство")

            DownloadResult.Success(savedUri, resolvedName, mimeType)
        } catch (e: Exception) {
            DownloadResult.Error(e.localizedMessage ?: "Ошибка скачивания файла")
        }
    }

    private fun saveToDownloads(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        mimeType: String
    ): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                resolver.openOutputStream(uri)?.use { output ->
                    inputStream.copyTo(output)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                var file = File(downloadsDir, fileName)
                var counter = 1
                val base = fileName.substringBeforeLast('.')
                val ext = fileName.substringAfterLast('.', "")
                while (file.exists()) {
                    val newName = if (ext.isNotBlank()) "${base}_$counter.$ext" else "${base}_$counter"
                    file = File(downloadsDir, newName)
                    counter++
                }

                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                Uri.fromFile(file)
            }
        } catch (_: Exception) {
            // Резервное сохранение в файлы приложения с FileProvider
            try {
                val cacheDir = File(context.cacheDir, "downloads").apply { if (!exists()) mkdirs() }
                val file = File(cacheDir, fileName)
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun openFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Не найдено приложение для открытия файла", Toast.LENGTH_SHORT).show()
        }
    }
}
