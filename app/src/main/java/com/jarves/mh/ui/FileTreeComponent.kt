package com.jarves.mh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.model.WorkspaceEntry

/**
 * Context action menu for file tree items.
 */
@Composable
fun FileEntryContextMenu(
    entry: WorkspaceEntry,
    onNewFileInDir: (String) -> Unit = {},
    onNewFolderInDir: (String) -> Unit = {},
    onRename: (WorkspaceEntry) -> Unit,
    onDuplicate: (WorkspaceEntry) -> Unit,
    onDelete: (WorkspaceEntry) -> Unit,
    onCopyPath: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = Modifier.size(28.dp),
    ) {
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "Options",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        if (entry.isDirectory) {
            DropdownMenuItem(
                text = { Text("New file inside") },
                leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null, Modifier.size(18.dp)) },
                onClick = {
                    expanded = false
                    onNewFileInDir(entry.path)
                },
            )
            DropdownMenuItem(
                text = { Text("New folder inside") },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, Modifier.size(18.dp)) },
                onClick = {
                    expanded = false
                    onNewFolderInDir(entry.path)
                },
            )
        } else {
            DropdownMenuItem(
                text = { Text("Duplicate") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(18.dp)) },
                onClick = {
                    expanded = false
                    onDuplicate(entry)
                },
            )
        }

        DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, Modifier.size(18.dp)) },
            onClick = {
                expanded = false
                onRename(entry)
            },
        )

        DropdownMenuItem(
            text = { Text("Copy relative path") },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(18.dp)) },
            onClick = {
                expanded = false
                onCopyPath(entry.path)
            },
        )

        DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error) },
            onClick = {
                expanded = false
                onDelete(entry)
            },
        )
    }
}

/**
 * Dialog to create a new file with parent folder context.
 */
@Composable
fun CreateFileDialog(
    parentDirectory: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var fileName by rememberSaveable { mutableStateOf("") }
    val displayParent = parentDirectory?.takeIf { it.isNotBlank() } ?: "project root"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Creating in: $displayParent",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it.trim() },
                    singleLine = true,
                    label = { Text("File name (e.g. Main.kt)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fullPath = if (!parentDirectory.isNullOrBlank()) {
                        "$parentDirectory/$fileName"
                    } else {
                        fileName
                    }
                    onConfirm(fullPath)
                },
                enabled = fileName.isNotBlank() && !fileName.contains("//"),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Dialog to create a new folder with parent folder context.
 */
@Composable
fun CreateFolderDialog(
    parentDirectory: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var folderName by rememberSaveable { mutableStateOf("") }
    val displayParent = parentDirectory?.takeIf { it.isNotBlank() } ?: "project root"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Creating in: $displayParent",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it.trim() },
                    singleLine = true,
                    label = { Text("Folder name (e.g. models)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fullPath = if (!parentDirectory.isNullOrBlank()) {
                        "$parentDirectory/$folderName"
                    } else {
                        folderName
                    }
                    onConfirm(fullPath)
                },
                enabled = folderName.isNotBlank() && !folderName.contains("//"),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Dialog to rename an existing file or directory.
 */
@Composable
fun RenameEntryDialog(
    entry: WorkspaceEntry,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var newName by rememberSaveable { mutableStateOf(entry.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry.isDirectory) "Rename Folder" else "Rename File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Path: ${entry.path}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.trim() },
                    singleLine = true,
                    label = { Text("New name") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parent = entry.path.substringBeforeLast('/', "")
                    val newRelativePath = if (parent.isNotBlank()) "$parent/$newName" else newName
                    onConfirm(newRelativePath)
                },
                enabled = newName.isNotBlank() && newName != entry.name && !newName.contains('/'),
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/**
 * Dialog to confirm deletion of a file or folder.
 */
@Composable
fun DeleteEntryDialog(
    entry: WorkspaceEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(if (entry.isDirectory) "Delete Folder?" else "Delete File?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Are you sure you want to delete \"${entry.name}\"?",
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = entry.path,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (entry.isDirectory) {
                    Text(
                        text = "This will delete all files and subdirectories inside this folder permanently.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
