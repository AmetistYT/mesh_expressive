package ru.mesh.expressive.data.local

import android.util.Base64
import org.json.JSONObject

object TokenUtils {
    fun isJwtExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            var payload = parts[1]
            val padLength = (4 - (payload.length % 4)) % 4
            if (padLength in 1..3) {
                payload += "=".repeat(padLength)
            }
            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.DEFAULT
            )
            val json = String(decodedBytes, Charsets.UTF_8)
            val obj = JSONObject(json)
            if (obj.has("exp")) {
                val expSeconds = obj.getLong("exp")
                val expMillis = expSeconds * 1000L
                System.currentTimeMillis() >= (expMillis - 30_000L)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isTokenValidAndNotExpired(token: String?): Boolean {
        if (token.isNullOrBlank() || token.length <= 20) return false
        if (isJwtExpired(token)) return false
        return true
    }

    fun extractValidTokenFromCookies(cookies: String): String? {
        val map = cookies.split(";").mapNotNull {
            val parts = it.split("=")
            if (parts.size >= 2) parts[0].trim() to parts.subList(1, parts.size).joinToString("=").trim() else null
        }.toMap()

        val candidates = listOfNotNull(
            map["auth_token"],
            map["mes_session"],
            map["aupd_token"]
        )

        for (token in candidates) {
            if (isTokenValidAndNotExpired(token)) {
                return token
            }
        }

        return null
    }
}
