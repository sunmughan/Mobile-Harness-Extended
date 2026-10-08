package com.jarves.mh.workspace

import java.io.File

/**
 * Controller for safe, interactive workspace file tree operations (create, rename, delete, duplicate, move)
 * with directory traversal protection and atomic crash-safe writes.
 */
class FileOperationsController {

    sealed class FileOpError(message: String) : Exception(message) {
        class SecurityViolation(msg: String) : FileOpError(msg)
        class TargetAlreadyExists(msg: String) : FileOpError(msg)
        class SourceNotFound(msg: String) : FileOpError(msg)
        class InvalidPath(msg: String) : FileOpError(msg)
        class OperationFailed(msg: String) : FileOpError(msg)
    }

    /**
     * Resolves and verifies that [relativePath] stays strictly within [workspaceRoot].
     * Throws [FileOpError.SecurityViolation] or [FileOpError.InvalidPath] on illegal access.
     */
    fun resolveWithinWorkspace(workspaceRoot: File, relativePath: String): Result<File> = runCatching {
        val cleanRel = relativePath.trim().replace('\\', '/').trimStart('/')
        if (cleanRel.isBlank()) {
            throw FileOpError.InvalidPath("Relative path cannot be empty")
        }
        val canonicalRoot = workspaceRoot.canonicalFile
        val candidate = File(canonicalRoot, cleanRel).canonicalFile
        if (!candidate.toPath().startsWith(canonicalRoot.toPath())) {
            throw FileOpError.SecurityViolation("Path traversal attempt detected: $relativePath")
        }
        candidate
    }

    /**
     * Safely creates a new file at [relativePath] with optional [initialContent].
     */
    fun createFile(
        workspaceRoot: File,
        relativePath: String,
        initialContent: String = "",
    ): Result<File> = runCatching {
        val file = resolveWithinWorkspace(workspaceRoot, relativePath).getOrThrow()
        if (file.exists()) {
            throw FileOpError.TargetAlreadyExists("File or directory already exists: $relativePath")
        }
        val parent = file.parentFile ?: throw FileOpError.OperationFailed("Cannot determine parent directory")
        if (!parent.exists() && !parent.mkdirs()) {
            throw FileOpError.OperationFailed("Failed to create parent directories for: $relativePath")
        }
        val success = SafeFileOps.atomicWriteText(file, initialContent)
        if (!success) {
            throw FileOpError.OperationFailed("Failed to atomically write new file: $relativePath")
        }
        file
    }

    /**
     * Safely creates a new directory at [relativePath].
     */
    fun createDirectory(workspaceRoot: File, relativePath: String): Result<File> = runCatching {
        val dir = resolveWithinWorkspace(workspaceRoot, relativePath).getOrThrow()
        if (dir.exists()) {
            throw FileOpError.TargetAlreadyExists("Directory already exists: $relativePath")
        }
        if (!dir.mkdirs()) {
            throw FileOpError.OperationFailed("Failed to create directory: $relativePath")
        }
        dir
    }

    /**
     * Safely renames [oldRelativePath] to [newRelativePath].
     */
    fun renameEntry(
        workspaceRoot: File,
        oldRelativePath: String,
        newRelativePath: String,
    ): Result<File> = runCatching {
        val source = resolveWithinWorkspace(workspaceRoot, oldRelativePath).getOrThrow()
        if (!source.exists()) {
            throw FileOpError.SourceNotFound("Source file or folder not found: $oldRelativePath")
        }
        val destination = resolveWithinWorkspace(workspaceRoot, newRelativePath).getOrThrow()
        if (destination.exists()) {
            throw FileOpError.TargetAlreadyExists("Destination already exists: $newRelativePath")
        }
        val destParent = destination.parentFile
        if (destParent != null && !destParent.exists() && !destParent.mkdirs()) {
            throw FileOpError.OperationFailed("Failed to create parent folder for destination")
        }

        val renamed = source.renameTo(destination)
        if (!renamed) {
            // Fall back to copy and delete if across mount points
            if (source.isDirectory) {
                source.copyRecursively(destination, overwrite = false)
                source.deleteRecursively()
            } else {
                source.copyTo(destination, overwrite = false)
                source.delete()
            }
        }
        destination
    }

    /**
     * Safely deletes [relativePath]. Refuses to delete the workspace root itself.
     */
    fun deleteEntry(workspaceRoot: File, relativePath: String): Result<Boolean> = runCatching {
        val target = resolveWithinWorkspace(workspaceRoot, relativePath).getOrThrow()
        val canonicalRoot = workspaceRoot.canonicalFile
        if (target == canonicalRoot) {
            throw FileOpError.SecurityViolation("Cannot delete the workspace root directory")
        }
        if (!target.exists()) {
            throw FileOpError.SourceNotFound("File or directory not found: $relativePath")
        }
        if (target.isDirectory) {
            target.deleteRecursively()
        } else {
            target.delete()
        }
    }

    /**
     * Duplicates a file, e.g. `foo.kt` -> `foo_copy.kt` or `foo_copy_2.kt`.
     */
    fun duplicateEntry(workspaceRoot: File, relativePath: String): Result<File> = runCatching {
        val source = resolveWithinWorkspace(workspaceRoot, relativePath).getOrThrow()
        if (!source.isFile) {
            throw FileOpError.SourceNotFound("Source file not found or is a directory: $relativePath")
        }
        val parent = source.parentFile ?: throw FileOpError.OperationFailed("Cannot determine parent directory")
        val baseName = source.nameWithoutExtension
        val ext = source.extension.let { if (it.isNotBlank()) ".$it" else "" }

        var copyName = "${baseName}_copy$ext"
        var candidate = File(parent, copyName)
        var counter = 2
        while (candidate.exists()) {
            copyName = "${baseName}_copy_$counter$ext"
            candidate = File(parent, copyName)
            counter++
        }

        val content = source.readBytes()
        val written = SafeFileOps.atomicWriteBytes(candidate, content)
        if (!written) {
            throw FileOpError.OperationFailed("Failed to write duplicate file: ${candidate.name}")
        }
        candidate
    }

    /**
     * Moves [sourceRelativePath] into directory [targetDirectoryRelativePath].
     */
    fun moveEntry(
        workspaceRoot: File,
        sourceRelativePath: String,
        targetDirectoryRelativePath: String,
    ): Result<File> = runCatching {
        val source = resolveWithinWorkspace(workspaceRoot, sourceRelativePath).getOrThrow()
        if (!source.exists()) {
            throw FileOpError.SourceNotFound("Source entry not found: $sourceRelativePath")
        }
        val targetDir = if (targetDirectoryRelativePath.isBlank() || targetDirectoryRelativePath == ".") {
            workspaceRoot.canonicalFile
        } else {
            resolveWithinWorkspace(workspaceRoot, targetDirectoryRelativePath).getOrThrow()
        }
        if (!targetDir.isDirectory) {
            throw FileOpError.InvalidPath("Target is not a directory: $targetDirectoryRelativePath")
        }

        val destination = File(targetDir, source.name)
        if (destination.exists()) {
            throw FileOpError.TargetAlreadyExists("An item named ${source.name} already exists in ${targetDir.name}")
        }

        val moved = source.renameTo(destination)
        if (!moved) {
            if (source.isDirectory) {
                source.copyRecursively(destination, overwrite = false)
                source.deleteRecursively()
            } else {
                source.copyTo(destination, overwrite = false)
                source.delete()
            }
        }
        destination
    }
}
