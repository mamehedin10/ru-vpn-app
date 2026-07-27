package com.v2ray.ang

import com.v2ray.ang.handler.AngConfigManager
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.LogUtil

/**
 * Bakes the Breeze RU server into the app on first launch (and refreshes it
 * whenever CONFIG_VERSION changes) so the user only has to install and connect.
 *
 * VLESS + Vision + Reality on a clean Aeza (Finland) IP + TLS fragment. Russia
 * throttles some providers (e.g. Hetzner) at the IP-range level but not Aeza;
 * Reality + fragment add DPI camouflage. Verified working through a real Russian
 * consumer mobile network (Rostelecom): Facebook/YouTube 5/5.
 */
object PrebakedConfig {

    // Bump this string whenever LINK changes so old installs get the new server
    // even when the user installs over the previous version (data not cleared).
    private const val CONFIG_VERSION = "aeza-reality-frag-1"
    private const val VERSION_KEY = "breeze_config_version"

    private const val LINK =
        "vless://497ea5af-1fd6-4f06-b909-6fe77995ee30@109.120.185.58:443" +
            "?encryption=none&security=reality&flow=xtls-rprx-vision" +
            "&sni=www.apple.com&fp=chrome" +
            "&pbk=ePsJg6vf5SHCTpVY0o-E7IXBhueFeCSAX8qR-7KnATw" +
            "&sid=985f8565714f1410&type=tcp&headerType=none#Breeze-RU"

    fun ensureInstalled() {
        try {
            // Enable TLS fragment (defeats Russia's consumer DPI / SNI throttling).
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_ENABLED, true)
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_PACKETS, "tlshello")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_LENGTH, "50-100")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_INTERVAL, "10-20")
            MmkvManager.encodeSettings(AppConfig.PREF_FRAGMENT_MAXSPLIT, "10")

            val stored = MmkvManager.decodeSettingsString(VERSION_KEY)
            val hasServers = MmkvManager.decodeAllServerList().isNotEmpty()

            if (stored != CONFIG_VERSION) {
                // New/changed baked config — wipe any stale server(s) and import fresh
                // so an install-over-old-version still ends up on the current server.
                if (hasServers) MmkvManager.removeAllServer()
                importAndSelect()
                MmkvManager.encodeSettings(VERSION_KEY, CONFIG_VERSION)
            } else if (!hasServers) {
                importAndSelect()
            }
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to install prebaked config", e)
        }
    }

    private fun importAndSelect() {
        val (count, _) = AngConfigManager.importBatchConfig(LINK, "", true)
        if (count > 0) {
            MmkvManager.decodeServerList("").firstOrNull()?.let { guid ->
                MmkvManager.setSelectServer(guid)
            }
        }
    }
}
