package com.codeair.mhe.operator.execution

import com.codeair.mhe.operator.observation.UiNode
import org.junit.Assert.*
import org.junit.Test

class GoalTargetResolverTest {

    private fun node(
        text: String? = null,
        desc: String? = null,
        clickable: Boolean = false,
        children: List<UiNode> = emptyList()
    ) = UiNode(
        nodeId = "g1:p0",
        className = "android.widget.Button",
        text = text,
        contentDescription = desc,
        resourceId = null,
        bounds = android.graphics.Rect(0, 0, 100, 100),
        clickable = clickable,
        scrollable = false,
        editable = false,
        children = children
    )

    @Test
    fun exactTextMatch_scoresHighest() {
        val loginBtn = node(text = "Login", clickable = true)
        val otherBtn = node(text = "Cancel", clickable = true)
        val root = node(children = listOf(otherBtn, loginBtn))
        val res = resolveTapTarget("tap the login button", root)
        assertNotNull(res.node)
        assertEquals("Login", res.node!!.text)
    }

    @Test
    fun noMatch_returnsNull() {
        val btn = node(text = "Settings", clickable = true)
        val root = node(children = listOf(btn))
        val res = resolveTapTarget("tap the login button", root)
        assertNull(res.node)
    }

    @Test
    fun ambiguousMatch_returnsNull() {
        val btn1 = node(text = "Login", clickable = true)
        val btn2 = node(text = "Login", clickable = true)
        val root = node(children = listOf(btn1, btn2))
        val res = resolveTapTarget("tap login", root)
        assertNull(res.node)
    }

    @Test
    fun nullRoot_returnsNull() {
        val res = resolveTapTarget("tap login", null)
        assertNull(res.node)
    }

    @Test
    fun contentDescription_matching_works() {
        val btn = node(desc = "Sign in button", clickable = true)
        val root = node(children = listOf(btn))
        val res = resolveTapTarget("tap sign in", root)
        assertNotNull(res.node)
    }
}
