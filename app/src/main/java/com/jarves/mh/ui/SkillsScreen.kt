package com.jarves.mh.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarves.mh.runtime.SkillInfo
import com.jarves.mh.runtime.SkillSearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    installed: List<SkillInfo>,
    results: List<SkillSearchResult>,
    busy: Boolean,
    message: String?,
    skillUpdates: Set<String> = emptySet(),
    onSearch: (String) -> Unit,
    onImportGitHub: (String) -> Unit,
    onImportZip: (Uri) -> Unit,
    onRemove: (String) -> Unit,
    onCheckUpdates: () -> Unit = {},
    onUpdate: (String) -> Unit = {},

) {
    var query by rememberSaveable { mutableStateOf("") }
    var githubUrl by rememberSaveable { mutableStateOf("") }
    val zipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImportZip)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Skills", fontWeight = FontWeight.Bold)
                        Text("Reusable agent capabilities", fontSize = 11.sp)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Extension, null)
                            Spacer(Modifier.size(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Universal Agent Skills", fontWeight = FontWeight.Bold)
                                Text(
                                    "Import standard SKILL.md bundles once and make them available to the selected coding agent.",
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            placeholder = { Text("Search GitHub: security, android, react…") },
                        )
                        Button(
                            onClick = { onSearch(query) },
                            enabled = query.trim().length >= 2 && !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Search, null)
                            Spacer(Modifier.size(8.dp))
                            Text("Discover GitHub skills")
                        }
                        OutlinedTextField(
                            value = githubUrl,
                            onValueChange = { githubUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.CloudDownload, null) },
                            placeholder = { Text("https://github.com/owner/skill-repository") },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    onImportGitHub(githubUrl)
                                    githubUrl = ""
                                },
                                enabled = githubUrl.startsWith("https://github.com/") && !busy,
                                modifier = Modifier.weight(1f),
                            ) { Text("Import GitHub") }
                            Button(
                                onClick = { zipLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                                enabled = !busy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.FileUpload, null)
                                Spacer(Modifier.size(6.dp))
                                Text("Import ZIP")
                            }
                        }
                        message?.let { Text(it, fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = onCheckUpdates,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                            Spacer(Modifier.size(8.dp))
                            Text("Check for skill updates")
                        }
                    }
                }
            }

            if (installed.isNotEmpty()) {
                item { Text("Installed", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(installed, key = { it.name }) { skill ->
                    SkillCard(
                        skill = skill,
                        updateAvailable = skill.name in skillUpdates,
                        busy = busy,
                        onUpdate = { onUpdate(skill.name) },
                        onRemove = { onRemove(skill.name) },
                    )
                }
            }

            if (results.isNotEmpty()) {
                item { Text("GitHub results", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(results, key = { it.repositoryUrl }) { result ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null)
                            Spacer(Modifier.size(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(result.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(result.description, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                Text(result.repositoryUrl, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Button(onClick = { onImportGitHub(result.repositoryUrl) }, enabled = !busy) { Text("Import") }
                        }
                    }
                }
            }

            if (installed.isEmpty() && results.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("No skills installed yet. Discover a GitHub skill or import a custom ZIP.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillCard(
    skill: SkillInfo,
    updateAvailable: Boolean,
    busy: Boolean,
    onUpdate: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(skill.name, fontWeight = FontWeight.SemiBold)
                Text(skill.description, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(skill.source, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (updateAvailable && skill.source.startsWith("https://github.com/", ignoreCase = true)) {
                Button(onClick = onUpdate, enabled = !busy) { Text("Update") }
            }
            IconButton(onClick = onRemove, enabled = !busy) {
                Icon(Icons.Default.Delete, contentDescription = "Remove skill")
            }
        }
    }
}
