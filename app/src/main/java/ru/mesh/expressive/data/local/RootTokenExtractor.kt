package ru.mesh.expressive.data.local

import android.os.Process as AndroidProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object RootTokenExtractor {

    data class RootExtractResult(
        val isSuccess: Boolean,
        val token: String? = null,
        val message: String
    )

    suspend fun extractTokenFromOfficialApp(cacheDir: File): RootExtractResult = withContext(Dispatchers.IO) {
        val targetDir = File(cacheDir, "extracted_dnevnik").apply {
            deleteRecursively()
            mkdirs()
        }

        try {
            val myUid = AndroidProcess.myUid()
            // Copy files from official app to our own readable cache directory with app ownership
            val cmd = "cp -r /data/data/ru.mes.dnevnik/no_backup/ ${targetDir.absolutePath}/ 2>/dev/null; " +
                      "cp -r /data/data/ru.mes.dnevnik/shared_prefs/ ${targetDir.absolutePath}/ 2>/dev/null; " +
                      "cp -r /data/data/ru.mes.dnevnik/app_webview/ ${targetDir.absolutePath}/ 2>/dev/null; " +
                      "chown -R $myUid:$myUid ${targetDir.absolutePath} 2>/dev/null; " +
                      "chmod -R 777 ${targetDir.absolutePath} 2>/dev/null"

            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.waitFor()

            // Scan all copied files in Kotlin
            val jwtRegex = Regex("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}")
            var foundToken: String? = null

            targetDir.walkTopDown().forEach { file ->
                if (file.isFile && foundToken == null) {
                    try {
                        val content = file.readText(Charsets.UTF_8)
                        val match = jwtRegex.find(content)
                        if (match != null) {
                            foundToken = match.value
                        }
                    } catch (_: Exception) {}
                }
            }

            // Cleanup temp files
            targetDir.deleteRecursively()

            if (!foundToken.isNullOrBlank()) {
                RootExtractResult(
                    isSuccess = true,
                    token = foundToken,
                    message = "Токен успешно извлечен через Root!"
                )
            } else {
                RootExtractResult(
                    isSuccess = false,
                    message = "Активная сессия не найдена в файлах ru.mes.dnevnik. Убедитесь, что вы авторизованы в официальном приложении МЭШ."
                )
            }
        } catch (e: Exception) {
            RootExtractResult(
                isSuccess = false,
                message = "Ошибка при выполнении Root: ${e.localizedMessage}"
            )
        }
    }
}
