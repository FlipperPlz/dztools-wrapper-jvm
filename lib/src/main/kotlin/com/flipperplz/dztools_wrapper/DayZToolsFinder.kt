package com.flipperplz.dztools_wrapper

import java.io.File
import java.util.prefs.Preferences

object DayZToolsFinder {
    private var dayzToolsFolder: File? = null
    private var locateFailed           = false;
    private const val WRAPPER_REGISTRY = "Software\\FlipperPlz\\dz_wrapper"
    private const val BOHEMIA_REGISTRY = "Software\\Bohemia Interactive\\Dayz Tools"
    private const val STEAM_REGISTRY   = "Software\\Valve\\Steam"
    private const val DAYZ_TOOLS_APPID = 830640;

    fun getDayZToolsFolder(): File {
        return if(locateFailed) throw Exception("DayZ Tools not found on the system") else
        if(dayzToolsFolder == null) with(locateDayZTools()) {
            dayzToolsFolder = this
            return dayzToolsFolder ?: throw Exception("DayZ Tools not found on the system");
        } else dayzToolsFolder!!
    }

    private fun locateDayZTools(): File? {
        var homePath: String? = if(Preferences.userRoot().nodeExists(WRAPPER_REGISTRY)) Preferences.userRoot().node(WRAPPER_REGISTRY).get("tools_path", null) else null
        if(homePath == null) homePath = if(Preferences.userRoot().nodeExists(BOHEMIA_REGISTRY)) Preferences.userRoot().node(WRAPPER_REGISTRY).get("path", null) else null
        if(homePath == null) homePath = tryLocateFromSteam()
        if(homePath == null) homePath = tryElevatedBohemiaLookup()
        if(homePath == null) { locateFailed = true; return null }
        val home = File(homePath)

        return if(home.exists()) home else null
    }

    private fun tryLocateFromSteam(): String? {
        var node: Preferences? = if(Preferences.userRoot().nodeExists(STEAM_REGISTRY)) Preferences.userRoot().node(
            STEAM_REGISTRY) else null
        if (node == null) node = tryElevatedSteamLookup()
        if (node == null) return null

        var steamPath: String? = node.get("installPath", null)
        if(steamPath == null) steamPath = node.get("SteamPath", null)
        if(steamPath == null) return null

        return dayzToolsFromSteam(steamPath);
    }

    private fun dayzToolsFromSteam(steamPath: String): String? {
        val steamHome = File(steamPath)
        if(!steamHome.exists()) return null
        if(steamHome.isFile) return null

        val steamApps = File(steamHome, "steamapps\\common")
        if(!steamApps.exists()) return null
        if(steamApps.isFile) return null

        val dzToolsFolder = File(steamApps, "DayZ Tools")
        if(!dzToolsFolder.exists()) return null
        if(dzToolsFolder.isFile) return null

        return dzToolsFolder.absolutePath
    }

    private fun tryElevatedBohemiaLookup(): String? {
        TODO()
    }

    private fun tryElevatedSteamLookup():  Preferences? {
        TODO("Not yet implemented")
    }
}