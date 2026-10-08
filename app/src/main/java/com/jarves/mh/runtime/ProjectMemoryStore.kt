package com.jarves.mh.runtime

import com.jarves.mh.workspace.SafeFileOps
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class MemoryCategory {
    ARCHITECTURE,
    ERROR_FIX,
    USER_PREFERENCE,
    CONVENTION,
    DEPENDENCY,
}

data class ProjectMemoryItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val category: MemoryCategory,
    val key: String,
    val content: String,
    val confidence: Float = 1.0f,
    val timestampMillis: Long = System.currentTimeMillis(),
)

data class ProjectMemorySnapshot(
    val projectId: String,
    val items: List<ProjectMemoryItem>,
)

/**
 * Durable project memory store. Persists architectural rules, previous error fixes,
 * conventions, and preferences across sessions.
 */
class ProjectMemoryStore(private val baseDir: File) {

    private fun memoryFile(projectId: String): File =
        File(baseDir, "project_memory/$projectId.json")

    fun load(projectId: String): ProjectMemorySnapshot {
        val file = memoryFile(projectId)
        if (!file.isFile) return ProjectMemorySnapshot(projectId, emptyList())
        return runCatching {
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val array = json.optJSONArray("items") ?: JSONArray()
            val items = (0 until array.length()).mapNotNull { i ->
                array.optJSONObject(i)?.let { obj ->
                    ProjectMemoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString().take(8)),
                        category = runCatching { MemoryCategory.valueOf(obj.getString("category")) }.getOrDefault(MemoryCategory.CONVENTION),
                        key = obj.optString("key"),
                        content = obj.optString("content"),
                        confidence = obj.optDouble("confidence", 1.0).toFloat(),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                    )
                }
            }
            ProjectMemorySnapshot(projectId, items)
        }.getOrDefault(ProjectMemorySnapshot(projectId, emptyList()))
    }

    fun record(
        projectId: String,
        category: MemoryCategory,
        key: String,
        content: String,
        confidence: Float = 1.0f,
    ): ProjectMemoryItem {
        val existing = load(projectId)
        val filtered = existing.items.filterNot { it.category == category && it.key.equals(key, ignoreCase = true) }
        val newItem = ProjectMemoryItem(
            category = category,
            key = key,
            content = content,
            confidence = confidence,
        )
        val updatedList = (filtered + newItem).takeLast(MAX_ITEMS_PER_PROJECT)
        persist(projectId, updatedList)
        return newItem
    }

    fun getMemories(projectId: String, category: MemoryCategory? = null): List<ProjectMemoryItem> {
        val snapshot = load(projectId)
        return if (category == null) snapshot.items else snapshot.items.filter { it.category == category }
    }

    fun clear(projectId: String) {
        val file = memoryFile(projectId)
        if (file.exists()) file.delete()
    }

    private fun persist(projectId: String, items: List<ProjectMemoryItem>) {
        val file = memoryFile(projectId)
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("category", item.category.name)
                    .put("key", item.key)
                    .put("content", item.content)
                    .put("confidence", item.confidence.toDouble())
                    .put("timestampMillis", item.timestampMillis)
            )
        }
        val rootJson = JSONObject()
            .put("projectId", projectId)
            .put("items", array)

        SafeFileOps.atomicWriteText(file, rootJson.toString())
    }

    companion object {
        private const val MAX_ITEMS_PER_PROJECT = 100
    }
}
