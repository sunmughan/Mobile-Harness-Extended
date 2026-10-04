package com.jarves.mh.runtime

import android.content.Context
import android.os.StatFs
import android.system.Os
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONObject

data class EnvironmentManifestComponent(
    val id: String,
    val label: String,
    val version: String,
    val packageUrl: String,
    val sha256: String,
    val sizeBytes: Long,
    val status: String,
    val minRuntimeGeneration: Int,
    val activeLink: String,
    val healthCommand: String,
    val notes: String,
)

data class EnvironmentComponentState(
    val id: String,
    val label: String,
    val currentVersion: String?,
    val latestVersion: String?,
    val rollbackVersion: String?,
    val rollbackAvailable: Boolean,
    val packageSizeBytes: Long,
    val notes: String,
)

class EnvironmentUpdateManager(
    private val context: Context,
    private val runtime: RuntimeInstaller,
) {
    private val rootfs get() = runtime.installedRuntime().rootfs
    private val base get() = File(rootfs, "opt/pocket/environment")
    private val components get() = File(base, "components")
    private val backups get() = File(base, "backups")
    private val downloads get() = File(context.cacheDir, "environment-updates")

    fun parseManifest(body: String): List<EnvironmentManifestComponent> {
        val root = JSONObject(body)
        val array = root.optJSONArray("components") ?: return emptyList()
        val result = ArrayList<EnvironmentManifestComponent>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            result += EnvironmentManifestComponent(
                id = item.getString("id"),
                label = item.optString("label", item.getString("id")),
                version = item.getString("version"),
                packageUrl = item.getString("packageUrl"),
                sha256 = item.getString("sha256"),
                sizeBytes = item.optLong("sizeBytes", 0L),
                status = item.optString("status", "stable"),
                minRuntimeGeneration = item.optInt("minRuntimeGeneration", 1),
                activeLink = item.optString("activeLink", ""),
                healthCommand = item.optString("healthCommand", ""),
                notes = item.optString("notes", ""),
            )
        }
        return result
    }

    fun inspect(manifest: List<EnvironmentManifestComponent>): List<EnvironmentComponentState> =
        manifest.map { item ->
            val backup = File(backups, item.id + ".zip")
            EnvironmentComponentState(
                id = item.id,
                label = item.label,
                currentVersion = currentVersion(item.id),
                latestVersion = item.version.takeIf { it != currentVersion(item.id) },
                rollbackVersion = backupVersion(backup),
                rollbackAvailable = backup.isFile,
                packageSizeBytes = item.sizeBytes,
                notes = item.notes,
            )
        }

    suspend fun update(
        item: EnvironmentManifestComponent,
        onProgress: suspend (RuntimeInstallProgress) -> Unit,
    ) {
        require(item.packageUrl.startsWith("https://")) { "Environment package must use HTTPS" }
        require(item.status != "nightly") { "Nightly packages require an explicit development build" }
        require(item.minRuntimeGeneration <= 1) { "This package requires a newer runtime generation" }
        preflightStorage(item.sizeBytes)

        val old = currentVersion(item.id)
        if (old == item.version) return

        val componentRoot = File(components, item.id)
        val target = File(componentRoot, item.version)
        val staging = File(componentRoot, "." + item.version + ".installing")
        val packageFile = File(downloads, item.id + "-" + item.version + ".zip")

        staging.deleteRecursively()
        staging.mkdirs()
        downloads.mkdirs()

        onProgress(RuntimeInstallProgress("Downloading " + item.label + " " + item.version, 0.05f, indeterminate = true))
        download(item.packageUrl, packageFile, item.sha256) { done, total ->
            val fraction = if (total > 0L) done.toFloat() / total else 0f
            onProgress(RuntimeInstallProgress("Downloading " + item.label, 0.05f + fraction * 0.45f, done, total))
        }

        try {
            onProgress(RuntimeInstallProgress("Installing " + item.label + " " + item.version, 0.55f, indeterminate = true))
            extractZip(packageFile, staging)
            verifyPackage(staging, item)

            val backup = File(backups, item.id + ".zip")
            if (old != null) {
                val oldDir = File(componentRoot, old)
                check(oldDir.isDirectory) { "Previous " + item.label + " installation is missing" }
                zipDirectory(oldDir, backup, old)
            }

            target.deleteRecursively()
            check(staging.renameTo(target)) { "Could not stage " + item.label }
            activate(item, target)
            healthCheck(item, target)

            if (old != null) File(componentRoot, old).deleteRecursively()
            packageFile.delete()
            staging.deleteRecursively()
            onProgress(RuntimeInstallProgress(item.label + " " + item.version + " is ready", 1f, event = RuntimeInstallEvent.COMPLETED))
        } catch (error: Throwable) {
            staging.deleteRecursively()
            if (old != null && File(backups, item.id + ".zip").isFile) {
                runCatching { restore(item, old) }
            }
            throw IllegalStateException(item.label + " update failed: " + (error.message ?: "unknown error"), error)
        }
    }

    suspend fun rollback(
        item: EnvironmentManifestComponent,
        onProgress: suspend (RuntimeInstallProgress) -> Unit,
    ) {
        val backup = File(backups, item.id + ".zip")
        val version = backupVersion(backup) ?: error("No rollback backup is available for " + item.label)
        val current = currentVersion(item.id)
        val componentRoot = File(components, item.id)
        val restore = File(componentRoot, "." + version + ".restore")
        restore.deleteRecursively()
        restore.mkdirs()

        onProgress(RuntimeInstallProgress("Restoring " + item.label + " " + version, 0.15f, indeterminate = true))
        extractZip(backup, restore)
        verifyPackage(restore, item.copy(version = version))
        healthCheck(item.copy(version = version), restore)

        if (current != null && current != version) File(componentRoot, current).deleteRecursively()
        val active = File(componentRoot, version)
        active.deleteRecursively()
        check(restore.renameTo(active)) { "Could not finalize rollback" }
        activate(item.copy(version = version), active)
        backup.delete()
        onProgress(RuntimeInstallProgress(item.label + " rollback complete", 1f, event = RuntimeInstallEvent.COMPLETED))
    }

    fun cleanup() {
        downloads.listFiles().orEmpty().forEach { if (it.name.endsWith(".zip") || it.name.endsWith(".part")) it.delete() }
        components.listFiles().orEmpty().forEach { dir ->
            dir.listFiles().orEmpty().filter { it.name.contains(".installing") || it.name.contains(".restore") }.forEach(File::deleteRecursively)
        }
    }

    private fun currentVersion(id: String): String? =
        File(components, id).listFiles().orEmpty()
            .firstOrNull { it.isDirectory && !it.name.startsWith(".") }
            ?.name

    private fun backupVersion(file: File): String? {
        if (!file.isFile) return null
        return runCatching {
            ZipInputStream(FileInputStream(file)).use { zip ->
                val entry = zip.nextEntry ?: return@runCatching null
                if (entry.name != ".version") return@runCatching null
                zip.bufferedReader().readText().trim().takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }

    private fun restore(item: EnvironmentManifestComponent, version: String) {
        val backup = File(backups, item.id + ".zip")
        val root = File(components, item.id)
        val restore = File(root, "." + version + ".rollback")
        restore.deleteRecursively()
        restore.mkdirs()
        extractZip(backup, restore)
        root.listFiles().orEmpty().filter { it.isDirectory && !it.name.startsWith(".") }.forEach(File::deleteRecursively)
        val active = File(root, version)
        check(restore.renameTo(active)) { "Rollback extraction failed" }
        activate(item, active)
        healthCheck(item, active)
    }

    private fun activate(item: EnvironmentManifestComponent, target: File) {
        if (item.activeLink.isBlank()) return
        val link = File(rootfs, item.activeLink.removePrefix("/"))
        link.parentFile?.mkdirs()
        val path = link.toPath()
        if (java.nio.file.Files.exists(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) java.nio.file.Files.delete(path)
        Os.symlink(target.absolutePath, link.absolutePath)
    }

    private fun healthCheck(item: EnvironmentManifestComponent, installRoot: File) {
        check(installRoot.listFiles().orEmpty().isNotEmpty()) { item.label + " package is empty" }
        val marker = File(installRoot, ".component.json")
        check(marker.isFile) { item.label + " package metadata is missing" }
        val json = JSONObject(marker.readText())
        check(json.optString("id") == item.id) { item.label + " package id mismatch" }
        check(json.optString("version") == item.version) { item.label + " package version mismatch" }
    }

    private fun verifyPackage(root: File, item: EnvironmentManifestComponent) {
        healthCheck(item, root)
    }

    private fun preflightStorage(packageBytes: Long) {
        val available = StatFs(context.filesDir.absolutePath).availableBytes
        val required = packageBytes + 64L * 1024L * 1024L
        check(available >= required) {
            "Not enough storage. Need at least " + (required / (1024L * 1024L)) + " MB free."
        }
    }

    private fun zipDirectory(source: File, destination: File, version: String) {
        destination.parentFile?.mkdirs()
        val part = File(destination.parentFile, destination.name + ".part")
        part.delete()
        ZipOutputStream(FileOutputStream(part)).use { zip ->
            zip.putNextEntry(ZipEntry(".version"))
            zip.write(version.toByteArray())
            zip.closeEntry()
            source.walkTopDown().filter { it.isFile }.forEach { file ->
                val name = file.relativeTo(source).path.replace(File.separatorChar, '/')
                zip.putNextEntry(ZipEntry(name))
                FileInputStream(file).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        destination.delete()
        check(part.renameTo(destination)) { "Could not finalize rollback archive" }
    }

    private fun extractZip(archive: File, destination: File) {
        ZipInputStream(FileInputStream(archive)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val name = entry.name.removePrefix("./")
                check(name.isNotBlank() && !name.startsWith("/") && !name.contains("../")) { "Unsafe update archive path" }
                val target = File(destination, name)
                check(target.canonicalFile.toPath().startsWith(destination.canonicalFile.toPath())) { "Unsafe update archive path" }
                if (entry.isDirectory) target.mkdirs()
                else {
                    target.parentFile?.mkdirs()
                    FileOutputStream(target).use { zip.copyTo(it) }
                }
                entry = zip.nextEntry
            }
        }
    }

    private fun download(
        url: String,
        destination: File,
        expectedSha256: String,
        onBytes: suspend (Long, Long) -> Unit,
    ) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 120_000
        connection.instanceFollowRedirects = true
        check(connection.responseCode in 200..299) { "Environment download failed: HTTP " + connection.responseCode }
        val total = connection.contentLengthLong
        connection.inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(128 * 1024)
                var done = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    done += count
                    onBytes(done, total)
                }
            }
        }
        connection.disconnect()
        check(sha256(destination).equals(expectedSha256, true)) { "Environment package SHA-256 verification failed" }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(128 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
