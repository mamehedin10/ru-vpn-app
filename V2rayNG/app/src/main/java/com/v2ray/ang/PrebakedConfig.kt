package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes the Breeze RU server into the app on first launch so the user only has
 * to install and tap connect.
 *
 * VLESS + Vision + Reality on a clean Aeza (Finland) IP. Russia throttles some
 * providers (e.g. Hetzner) at the IP-range level, but not Aeza; Reality adds
 * DPI camouflage (looks like an apple.com TLS session). Verified working from
 * inside Russia: 76 Mbps, Facebook/YouTube/Google all 200, 5/5 consistent.
 */
object PrebakedConfig {

    private const val LINK =
        "vless://497ea5af-1fd6-4f06-b909-6fe77995ee30@109.120.185.58:443" +
            "?encryption=none&security=reality&flow=xtls-rprx-vision" +
            "&sni=www.apple.com&fp=chrome" +
            "&pbk=ePsJg6vf5SHCTpVY0o-E7IXBhueFeCSAX8qR-7KnATw" +
            "&sid=985f8565714f1410&type=tcp&headerType=none#Breeze-RU"

    /**
     * Imports the baked profile and selects it if no servers exist yet.
     * Safe to call on every launch — no-ops once a server is present.
     */
    fun ensureInstalled() {
        try {
            // Enable TLS fragment on every launch — splits the ClientHello so
            // Russia's consumer DPI (TSPU) can't read the SNI to throttle/block.
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_ENABLED, true)
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_PACKETS, "tlshello")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_LENGTH, "50-100")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_INTERVAL, "10-20")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_MAXSPLIT, "10")

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
