package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes fixed Breeze servers into the app on first launch so the friend only
 * has to install and tap connect. Two entries: CF (via Cloudflare) and Direct.
 */
object PrebakedConfig {

    // Breeze RU — via Cloudflare (connects to a Cloudflare edge IP, DNS-free)
    private const val LINK_CF =
        "vless://16f57e0d-e016-4677-8599-9bf20dd34a84@104.21.62.250:443" +
            "?encryption=none&security=tls&sni=cdn.breeze.rest" +
            "&type=ws&host=cdn.breeze.rest&path=%2Fru&fp=chrome#Breeze-CF"

    // Breeze RU — direct to origin
    private const val LINK_DIRECT =
        "vless://16f57e0d-e016-4677-8599-9bf20dd34a84@77.42.41.149:443" +
            "?encryption=none&security=tls&sni=ru.breeze.rest" +
            "&type=ws&host=ru.breeze.rest&path=%2Fru&fp=chrome#Breeze-Direct"

    /**
     * Imports the baked profiles and selects the CF one if no servers exist yet.
     * Safe to call on every launch — no-ops once a server is present.
     */
    fun ensureInstalled() {
        try {
            if (MmkvManager.decodeAllServerList().isNotEmpty()) return

            val batch = "$LINK_CF\n$LINK_DIRECT"
            val (count, _) = AngConfigManager.importBatchConfig(batch, "", true)
            if (count > 0) {
                MmkvManager.decodeServerList("").firstOrNull()?.let { guid ->
                    MmkvManager.setSelectServer(guid)
                }
            }
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to install prebaked config", e)
        }
    }
}
