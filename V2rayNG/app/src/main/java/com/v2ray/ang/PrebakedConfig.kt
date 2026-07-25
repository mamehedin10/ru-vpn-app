package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes the Breeze RU server into the app on first launch so the user only has
 * to install and tap connect. VLESS + WS + TLS via Cloudflare — bypasses
 * Russia's Hetzner-range throttling (Russia->Cloudflare is fast).
 */
object PrebakedConfig {

    // Breeze RU — VLESS + WS + TLS fronted by Cloudflare (cdn.breeze.rest)
    private const val LINK =
        "vless://94d252ad-2f62-4a67-bd37-5e006fa3af7b@cdn.breeze.rest:443" +
            "?encryption=none&security=tls&sni=cdn.breeze.rest" +
            "&type=ws&host=cdn.breeze.rest&path=%2Fru&fp=chrome#Breeze-RU"

    /**
     * Imports the baked profile and selects it if no servers exist yet.
     * Safe to call on every launch — no-ops once a server is present.
     */
    fun ensureInstalled() {
        try {
            if (MmkvManager.decodeAllServerList().isNotEmpty()) return

            val (count, _) = AngConfigManager.importBatchConfig(LINK, "", true)
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
