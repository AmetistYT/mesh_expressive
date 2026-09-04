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
            val packages = listOf("ru.mes.dnevnik", "ru.mesh.client", "ru.mesh.expressive")
            val copyCommands = packages.joinToString("; ") { pkg ->
                "cp -r /data/data/$pkg/no_backup/ ${targetDir.absolutePath}/$pkg 2>/dev/null; " +
                "cp -r /data/data/$pkg/shared_prefs/ ${targetDir.absolutePath}/$pkg 2>/dev/null; " +
                "cp -r /data/data/$pkg/app_webview/ ${targetDir.absolutePath}/$pkg 2>/dev/null"
            }
            val cmd = "$copyCommands; chown -R $myUid:$myUid ${targetDir.absolutePath} 2>/dev/null; chmod -R 777 ${targetDir.absolutePath} 2>/dev/null"

            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.waitFor()

            // Scan all copied files for JWT tokens and choose the freshest valid token
            val jwtRegex = Regex("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}")
            val candidateTokens = mutableListOf<String>()

            targetDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    try {
                        val content = file.readText(Charsets.UTF_8)
                        jwtRegex.findAll(content).forEach { match ->
                            candidateTokens.add(match.value)
                        }
                    } catch (_: Exception) {}
                }
            }

            // Cleanup temp files
            targetDir.deleteRecursively()

            val nowSeconds = System.currentTimeMillis() / 1000
            var bestToken: String? = null
            var bestExp = 0L

            for (tok in candidateTokens.distinct()) {
                val parts = tok.split(".")
                if (parts.size >= 2) {
                    try {
                        val decodedBytes = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING)
                        val jsonStr = String(decodedBytes, Charsets.UTF_8)
                        val json = org.json.JSONObject(jsonStr)
                        val exp = json.optLong("exp", 0L)
                        val iss = json.optString("iss", "")
                        if (iss.contains("school.mos.ru") && exp > nowSeconds) {
                            if (exp > bestExp) {
                                bestExp = exp
                                bestToken = tok
                            }
                        } else if (bestToken == null && iss.contains("school.mos.ru")) {
                            bestToken = tok
                        }
                    } catch (_: Exception) {}
                }
            }

            if (!bestToken.isNullOrBlank()) {
                RootExtractResult(
                    isSuccess = true,
                    token = bestToken,
                    message = "Токен успешно извлечен через Root!"
                )
            } else {
                RootExtractResult(
                    isSuccess = false,
                    message = "Активная сессия не найдена в файлах МЭШ. Убедитесь, что вы авторизованы в приложении."
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
