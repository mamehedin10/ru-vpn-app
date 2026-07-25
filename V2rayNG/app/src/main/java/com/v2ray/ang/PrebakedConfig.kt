package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes the Breeze RU server into the app on first launch so the user only has
 * to install and tap connect.
 *
 * VLESS + WS + TLS via Cloudflare. The server address is a Cloudflare edge IP
 * (not the domain) on purpose: Russian ISPs poison DNS for cdn.breeze.rest and
 * return the real origin IP, which Russia throttles. Connecting straight to a
 * Cloudflare IP bypasses DNS entirely and forces the traffic through Cloudflare
 * (which Russia does not throttle). SNI/Host still carry cdn.breeze.rest so CF
 * routes to our origin.
 */
object PrebakedConfig {

    private const val LINK =
        "vless://94d252ad-2f62-4a67-bd37-5e006fa3af7b@188.114.96.1:443" +
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
