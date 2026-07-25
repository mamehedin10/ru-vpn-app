package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes the Breeze RU server (VLESS + Vision + Reality) into the app on first
 * launch so the user only has to install and tap connect.
 */
object PrebakedConfig {

    // Breeze RU — VLESS + Vision + Reality (direct, DPI-camouflaged as apple.com)
    private const val LINK =
        "vless://94d252ad-2f62-4a67-bd37-5e006fa3af7b@77.42.41.149:443" +
            "?encryption=none&security=reality&flow=xtls-rprx-vision" +
            "&sni=www.apple.com&fp=chrome" +
            "&pbk=hXg6xyGKKzCRpyhihGaKG978H-NEWyq64laU04MqC2E" +
            "&sid=&type=tcp&headerType=none#Breeze-RU"

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
