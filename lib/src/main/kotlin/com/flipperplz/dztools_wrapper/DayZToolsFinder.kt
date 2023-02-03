package com.flipperplz.dztools_wrapper

import java.io.File
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.io.path.absolutePathString

object DayZToolsFinder {
    private var _dayzToolsFolder: File? = null
    private var _addonBuilderExe: File? = null
    private var _binarizeExe: File? = null
    private var _binMakeExe: File? = null
    private var _ceEditorExe: File? = null
    private var _cfgConvertExe: File? = null
    private var _dsUtilsCheckSignaturesExe: File? = null
    private var _dsUtilsCreateKeyExe: File? = null
    private var _dsUtilsSignFileExe: File? = null
    private var _imageToPaaExe: File? = null
    private var _navMeshGeneratorExe: File? = null
    private var _objectBuilderExe: File? = null
    private var _o2ScriptExe: File? = null
    private var _bankRevExe: File? = null
    private var _fileBankExe: File? = null
    private var _publisherExe: File? = null
    private var _terrainBuilderExe: File? = null
    private var _p3d2BmpExe: File? = null
    private var _terrainProcessorExe: File? = null
    private var _workbenchExe: File? = null

    private var locateFailed           = false;
    private const val WRAPPER_REGISTRY = "Software\\FlipperPlz\\dz-wrapper-jvm"
    private const val BOHEMIA_REGISTRY = "Software\\Bohemia Interactive\\Dayz Tools"
    private const val STEAM_REGISTRY   = "Software\\Valve\\Steam"
    private const val DZTOOLS_HOME_ENV = "DZTOOLS_HOME"
    private const val DAYZ_TOOLS_APPID = 830640;
    val dayzToolsFolder: File
        get() = if(locateFailed) throw Exception("DayZ Tools not found on the system") else
            if(_dayzToolsFolder == null) with(locateDayZTools()) {
                _dayzToolsFolder = this
                return dayzToolsFolder ?: throw Exception("DayZ Tools not found on the system");
            } else _dayzToolsFolder!!


    private fun locateDayZTools(): File? {
        var homeFile: File? = toolRootExistsOrNull(System.getProperty(DZTOOLS_HOME_ENV, null))
        if(homeFile == null) homeFile = toolRootExistsOrNull(if(Preferences.userRoot().nodeExists(WRAPPER_REGISTRY)) Preferences.userRoot().node(WRAPPER_REGISTRY).get("tools_path", null) else null)
        if(homeFile == null) homeFile = toolRootExistsOrNull(if(Preferences.userRoot().nodeExists(BOHEMIA_REGISTRY)) Preferences.userRoot().node(WRAPPER_REGISTRY).get("path", null) else null)
        if(homeFile == null) homeFile = toolRootExistsOrNull(tryLocateFromSteam())
        if(homeFile == null) homeFile = toolRootExistsOrNull(tryElevatedBohemiaLookup())
        if(homeFile == null) { locateFailed = true; return null }

        return homeFile.also { ensurePathSetup(homeFile) }
    }
    private fun ensurePathSetup(dayzToolsHome: File) {
        if(System.getProperty(DZTOOLS_HOME_ENV, null) == null)
            createEnvironmentalVariable(DZTOOLS_HOME_ENV, dayzToolsFolder.absolutePath)
        val node: Preferences = Preferences.userRoot().node(WRAPPER_REGISTRY)
        if(existsOrNull(node.get("toolsPath", null)) == null) node.put("toolsPath", dayzToolsFolder.absolutePath)
    }

    private fun validateToolsRoot(dayzToolsHome: File?): Boolean {
        if(dayzToolsHome == null) return false;
        val homePath = dayzToolsHome.absolutePath
        _addonBuilderExe = existsOrNull(Path.of(homePath, "Bin", "AddonBuilder", "AddonBuilder.exe").absolutePathString(), false) ?: return false
        _binarizeExe = existsOrNull(Path.of(homePath, "Bin", "Binarize", "binarize.exe").absolutePathString(), false) ?: return false
        _binMakeExe = existsOrNull(Path.of(homePath, "Bin", "BinMake", "binMake.exe").absolutePathString(), false) ?: return false
        _ceEditorExe = existsOrNull(Path.of(homePath, "Bin", "CeEditor", "CeEditor.exe").absolutePathString(), false) ?: return false
        _cfgConvertExe = existsOrNull(Path.of(homePath, "Bin", "CfgConvert", "CfgConvert.exe").absolutePathString(), false) ?: return false
        _dsUtilsCheckSignaturesExe = existsOrNull(Path.of(homePath, "Bin", "DsUtils", "DSCheckSignatures.exe").absolutePathString(), false) ?: return false
        _dsUtilsCreateKeyExe = existsOrNull(Path.of(homePath, "Bin", "DsUtils", "DSCreateKey.exe").absolutePathString(), false) ?: return false
        _dsUtilsSignFileExe = existsOrNull(Path.of(homePath, "Bin", "DsUtils", "DSSignFile.exe").absolutePathString(), false) ?: return false
        _imageToPaaExe = existsOrNull(Path.of(homePath, "Bin", "ImageToPAA", "ImageToPAA.exe").absolutePathString(), false) ?: return false
        _navMeshGeneratorExe = existsOrNull(Path.of(homePath, "Bin", "NavMeshGenerator", "NavMeshGenerator_x64.exe").absolutePathString(), false) ?: return false
        _objectBuilderExe = existsOrNull(Path.of(homePath, "Bin", "ObjectBuilder", "ObjectBuilder.exe").absolutePathString(), false) ?: return false
        _o2ScriptExe = existsOrNull(Path.of(homePath, "Bin", "ObjectBuilder", "O2Script.exe").absolutePathString(), false) ?: return false
        _bankRevExe = existsOrNull(Path.of(homePath, "Bin", "PboUtils", "BankRev.exe").absolutePathString(), false) ?: return false
        _fileBankExe = existsOrNull(Path.of(homePath, "Bin", "PboUtils", "FileBank.exe").absolutePathString(), false) ?: return false
        _publisherExe = existsOrNull(Path.of(homePath, "Bin", "Publisher", "Publisher.exe").absolutePathString(), false) ?: return false
        _terrainBuilderExe = existsOrNull(Path.of(homePath, "Bin", "TerrainBuilder", "TerrainBuilder.exe").absolutePathString(), false) ?: return false
        _p3d2BmpExe = existsOrNull(Path.of(homePath, "Bin", "TerrainBuilder", "p3d2bmp.exe").absolutePathString(), false) ?: return false
        _terrainProcessorExe = existsOrNull(Path.of(homePath, "Bin", "TerrainProcessor", "TerrainProcessor.exe").absolutePathString(), false) ?: return false
        _workbenchExe = existsOrNull(Path.of(homePath, "Bin", "Workbench", "workbenchApp.exe").absolutePathString(), false) ?: return false

        return true
    }

    private fun toolRootExistsOrNull(path: String?): File?  = with(existsOrNull(path, true)) {
        return if(this != null && validateToolsRoot(this)) this else null
    }

    private fun existsOrNull(path: String?, directory: Boolean = true): File? = if(path == null) null else {
        val file = File(path)
        if (file.exists() && file.isDirectory == directory) file
        else null
    }

    private fun createEnvironmentalVariable(key: String, value: String) = ProcessBuilder("cmd", "/c", "setx", key, value, "/M").start().waitFor()

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