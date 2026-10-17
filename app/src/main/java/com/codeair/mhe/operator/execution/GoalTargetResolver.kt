package com.codeair.mhe.operator.execution

import com.codeair.mhe.operator.observation.UiNode

data class TargetResolution(
    val node: UiNode?,
    val score: Int,
    val reason: String
)

fun resolveTapTarget(goalText: String, root: UiNode?): TargetResolution {
    if (root == null) return TargetResolution(null, 0, "no observation root")
    val keywords = extractKeywords(goalText)
    if (keywords.isEmpty()) return TargetResolution(null, 0, "no keywords in goal")
    
    val candidates = mutableListOf<Pair<UiNode, Int>>()
    collectClickableNodes(root, candidates, keywords)
    
    if (candidates.isEmpty()) return TargetResolution(null, 0, "no clickable nodes match goal keywords")
    
    candidates.sortByDescending { it.second }
    val best = candidates[0]
    
    if (candidates.size > 1 && candidates[1].second == best.second && best.second > 0) {
        return TargetResolution(null, 0, "ambiguous: multiple nodes match equally")
    }
    
    if (best.second == 0) {
        return TargetResolution(null, 0, "no node matches goal keywords")
    }
    
    return TargetResolution(best.first, best.second, "matched with score ${best.second}")
}

private fun extractKeywords(goalText: String): Set<String> {
    val stopwords = setOf("the", "a", "an", "to", "on", "in", "at", "for", "of", "and", "or", "tap", "click", "press", "open", "go", "please")
    return goalText.lowercase()
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .split(Regex("\\s+"))
        .filter { it.length > 2 && it !in stopwords }
        .toSet()
}

private fun collectClickableNodes(node: UiNode, out: MutableList<Pair<UiNode, Int>>, keywords: Set<String>) {
    if (node.clickable) {
        val score = scoreNode(node, keywords)
        out.add(node to score)
    }
    for (child in node.children) {
        collectClickableNodes(child, out, keywords)
    }
}

private fun scoreNode(node: UiNode, keywords: Set<String>): Int {
    val text = (node.text ?: "").lowercase()
    val desc = (node.contentDescription ?: "").lowercase()
    val combined = "$text $desc"
    var score = 0
    for (kw in keywords) {
        if (text == kw || desc == kw) score += 10
        else if (text.contains(kw) || desc.contains(kw)) score += 5
        else if (combined.split(Regex("\\s+")).any { it.startsWith(kw.take(4)) }) score += 2
    }
    return score
}
