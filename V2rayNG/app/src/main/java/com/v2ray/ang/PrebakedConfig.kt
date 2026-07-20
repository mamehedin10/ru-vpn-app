package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes a fixed server profile into the app on first launch so the user only
 * has to install and tap connect. No import / subscription step required.
 */
object PrebakedConfig {

    // Pre-loaded server (VLESS + Vision + Reality). Update this string to rotate the server.
    private const val LINK =
        "vless://601413a8-3cb8-4d01-b0fb-85a045ac1881@70.34.215.102:443" +
            "?encryption=none&flow=xtls-rprx-vision&security=reality" +
            "&sni=www.apple.com&fp=chrome" +
            "&pbk=NfdvTSju7RVjAtXLRUNHRLffoRiBWuivCxcwNz6vDjI" +
            "&sid=6ba85179e30d4fc2&type=tcp&headerType=none#RU-VPN"

    /**
     * Imports the baked profile and marks it selected if no servers exist yet.
     * Safe to call on every launch — it no-ops once a server is present.
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
