package com.jarves.mh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.max

private data class EditorSymbol(val name: String, val line: Int, val offset: Int)
private data class BracketPair(val cursor: Int, val match: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    filePath: String,
    content: String?,
    loading: Boolean,
    onClose: () -> Unit,
    onSave: (String) -> Unit,
) {
    val source = content.orEmpty()
    val readOnly = loading || source.contains("[File truncated — too large to display fully]")
    var value by remember(filePath) { mutableStateOf(TextFieldValue(source)) }
    var savedText by remember(filePath) { mutableStateOf(source) }
    var searchOpen by rememberSaveable(filePath) { mutableStateOf(false) }
    var menuOpen by rememberSaveable(filePath) { mutableStateOf(false) }
    var outlineOpen by rememberSaveable(filePath) { mutableStateOf(false) }
    var goToLineOpen by rememberSaveable(filePath) { mutableStateOf(false) }
    var find by rememberSaveable(filePath) { mutableStateOf("") }
    var replace by rememberSaveable(filePath) { mutableStateOf("") }
    var lineInput by rememberSaveable(filePath) { mutableStateOf("") }
    var bracketPair by remember(filePath) { mutableStateOf<BracketPair?>(null) }
    var saving by rememberSaveable(filePath) { mutableStateOf(false) }
    val vertical = rememberScrollState()
    val horizontal = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(source, filePath) {
        value = TextFieldValue(source)
        savedText = source
        bracketPair = null
    }

    val dirty = value.text != savedText
    val lines = remember(value.text) { value.text.split('\n') }
    val lineNumbers = remember(lines.size) { (1..max(1, lines.size)).joinToString("\n") }
    val symbols = remember(value.text) { extractEditorSymbols(value.text) }

    fun goToLine(line: Int) {
        val target = line.coerceIn(1, max(1, lines.size))
        val offset = value.text.split('\n')
            .take(target - 1)
            .sumOf { it.length + 1 }
            .coerceIn(0, value.text.length)
        value = value.copy(selection = TextRange(offset))
        scope.launch { vertical.animateScrollTo(((target - 1) * 20 * 3).coerceAtLeast(0)) }
    }

    fun replaceAll() {
        if (find.isBlank() || readOnly) return
        val replaced = value.text.replace(find, replace, ignoreCase = true)
        if (replaced != value.text) {
            val cursor = value.selection.start.coerceAtMost(replaced.length)
            value = TextFieldValue(replaced, TextRange(cursor))
            bracketPair = findBracketPair(replaced, cursor)
        }
    }

    if (outlineOpen) {
        ModalBottomSheet(onDismissRequest = { outlineOpen = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .padding(horizontal = 18.dp),
            ) {
                Text("Symbol outline", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    symbols.size.toString() + " declarations in " + filePath,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                if (symbols.isEmpty()) {
                    Text(
                        "No common class/function declarations detected.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                    ) {
                        items(symbols.size, key = { symbols[it].offset }) { index ->
                            val symbol = symbols[index]
                            TextButton(
                                onClick = {
                                    goToLine(symbol.line)
                                    outlineOpen = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(symbol.name)
                                    Text(
                                        "Line " + symbol.line,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (goToLineOpen) {
        AlertDialog(
            onDismissRequest = { goToLineOpen = false },
            title = { Text("Go to line") },
            text = {
                OutlinedTextField(
                    value = lineInput,
                    onValueChange = { lineInput = it.filter(Char::isDigit).take(7) },
                    singleLine = true,
                    label = { Text("Line number") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lineInput.toIntOrNull()?.let(::goToLine)
                        goToLineOpen = false
                    },
                    enabled = lineInput.toIntOrNull()?.let { it > 0 } == true,
                ) { Text("Go") }
            },
            dismissButton = { TextButton(onClick = { goToLineOpen = false }) { Text("Cancel") } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(filePath.substringAfterLast('/'), style = MaterialTheme.typography.titleMedium)
                        Text(filePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close editor") }
                },
                actions = {
                    if (dirty && !readOnly) {
                        IconButton(
                            enabled = !saving,
                            onClick = {
                                val textToSave = value.text
                                scope.launch {
                                    saving = true
                                    if (onSave(textToSave)) savedText = textToSave
                                    saving = false
                                }
                            },
                        ) {
                            if (saving) {
                                CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Save, "Save file", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    IconButton(onClick = { searchOpen = !searchOpen }) {
                        Icon(Icons.Default.Search, "Find and replace")
                    }
                    IconButton(onClick = { menuOpen = !menuOpen }) {
                        Icon(Icons.Default.MoreVert, "Editor actions")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Symbol outline") },
                            onClick = {
                                menuOpen = false
                                outlineOpen = true
                            },
                            leadingIcon = { Icon(Icons.Default.FormatListBulleted, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Go to line") },
                            onClick = {
                                menuOpen = false
                                goToLineOpen = true
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (searchOpen) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = find,
                            onValueChange = { find = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Find") },
                        )
                        OutlinedTextField(
                            value = replace,
                            onValueChange = { replace = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Replace") },
                        )
                        TextButton(
                            onClick = ::replaceAll,
                            enabled = find.isNotBlank() && !readOnly,
                        ) { Text("All") }
                    }
                }
            }

            if (readOnly && !loading) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "Read-only preview. This file is too large to edit safely.",
                        Modifier.padding(11.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(48.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .verticalScroll(vertical, enabled = false),
                        ) {
                            Text(
                                lineNumbers,
                                Modifier.padding(top = 8.dp, end = 6.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxSize()
                                .horizontalScroll(horizontal)
                                .verticalScroll(vertical),
                        ) {
                            BasicTextField(
                                value = value,
                                onValueChange = {
                                    if (!readOnly) {
                                        value = it
                                        bracketPair = findBracketPair(it.text, it.selection.start)
                                    }
                                },
                                modifier = Modifier
                                    .widthIn(min = 720.dp)
                                    .padding(start = 12.dp, top = 8.dp, end = 24.dp, bottom = 24.dp),
                                enabled = !readOnly,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                visualTransformation = CodeSyntaxVisualTransformation(
                                    keywordColor = MaterialTheme.colorScheme.primary,
                                    stringColor = MaterialTheme.colorScheme.tertiary,
                                    commentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    numberColor = MaterialTheme.colorScheme.secondary,
                                    bracketPair = bracketPair,
                                ),
                            )
                        }
                    }
                }
            }

            Text(
                statusText(value, dirty, bracketPair),
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun statusText(value: TextFieldValue, dirty: Boolean, bracketPair: BracketPair?): String {
    val line = value.text.take(value.selection.start.coerceIn(0, value.text.length)).count { it == '\n' } + 1
    return buildString {
        append("Ln ")
        append(line)
        append("  ·  ")
        append(value.text.length)
        append(" chars")
        if (dirty) append("  ·  Unsaved")
        if (bracketPair != null) {
            append("  ·  Match line ")
            append(value.text.take(bracketPair.match).count { it == '\n' } + 1)
        }
    }
}

private fun extractEditorSymbols(text: String): List<EditorSymbol> {
    val patterns = listOf(
        Regex("^\\s*(?:data\\s+|enum\\s+)?(?:class|interface|object)\\s+([A-Za-z_][A-Za-z0-9_]*)"),
        Regex("^\\s*(?:suspend\\s+)?fun\\s+([A-Za-z_][A-Za-z0-9_]*)"),
        Regex("^\\s*(?:def|async\\s+def)\\s+([A-Za-z_][A-Za-z0-9_]*)"),
        Regex("^\\s*(?:export\\s+)?(?:async\\s+)?function\\s+([A-Za-z_][A-Za-z0-9_]*)"),
    )
    val result = mutableListOf<EditorSymbol>()
    var offset = 0
    text.split('\n').forEachIndexed { index, line ->
        patterns.asSequence()
            .mapNotNull { it.find(line)?.groupValues?.getOrNull(1) }
            .firstOrNull()
            ?.let { result += EditorSymbol(it, index + 1, offset) }
        offset += line.length + 1
    }
    return result
}

private fun findBracketPair(text: String, cursor: Int): BracketPair? {
    val candidate = sequenceOf(cursor, cursor - 1)
        .filter { it in text.indices }
        .firstOrNull { text[it] in "()[]{}" } ?: return null
    val opening = text[candidate] in "([{"
    val current = text[candidate]
    val counterpart = when (current) {
        '(' -> ')'
        ')' -> '('
        '[' -> ']'
        ']' -> '['
        '{' -> '}'
        '}' -> '{'
        else -> return null
    }
    var depth = 0
    if (opening) {
        for (i in candidate until text.length) {
            if (text[i] == current) depth++
            if (text[i] == counterpart) {
                depth--
                if (depth == 0) return BracketPair(candidate, i)
            }
        }
    } else {
        for (i in candidate downTo 0) {
            if (text[i] == current) depth++
            if (text[i] == counterpart) {
                depth--
                if (depth == 0) return BracketPair(candidate, i)
            }
        }
    }
    return null
}

private class CodeSyntaxVisualTransformation(
    private val keywordColor: Color,
    private val stringColor: Color,
    private val commentColor: Color,
    private val numberColor: Color,
    private val bracketPair: BracketPair?,
) : VisualTransformation {
    private val keywords = setOf(
        "class", "interface", "object", "fun", "data", "enum", "sealed", "public", "private",
        "protected", "internal", "override", "suspend", "return", "if", "else", "when", "for",
        "while", "do", "try", "catch", "finally", "throw", "val", "var", "const", "let",
        "function", "async", "await", "import", "from", "export", "package", "extends", "implements",
        "new", "this", "super", "true", "false", "null", "def", "lambda",
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val builder = AnnotatedString.Builder()
        var global = 0
        source.split('\n').forEachIndexed { lineIndex, line ->
            var i = 0
            while (i < line.length) {
                val absolute = global + i
                val ch = line[i]
                if (ch == '#' || (ch == '/' && i + 1 < line.length && line[i + 1] == '/')) {
                    builder.pushStyle(SpanStyle(color = commentColor))
                    builder.append(line.substring(i))
                    builder.pop()
                    break
                }
                if (ch == '"' || ch == '\'') {
                    val start = i
                    val quote = ch
                    i++
                    while (i < line.length) {
                        if (line[i] == '\\') {
                            i = (i + 2).coerceAtMost(line.length)
                        } else if (line[i] == quote) {
                            i++
                            break
                        } else {
                            i++
                        }
                    }
                    builder.pushStyle(SpanStyle(color = stringColor))
                    builder.append(line.substring(start, i))
                    builder.pop()
                    continue
                }
                if (ch.isDigit()) {
                    val start = i
                    while (i < line.length && (line[i].isDigit() || line[i] == '.' || line[i] in "abcdefABCDEFxX")) i++
                    builder.pushStyle(SpanStyle(color = numberColor))
                    builder.append(line.substring(start, i))
                    builder.pop()
                    continue
                }
                if (ch.isLetter() || ch == '_') {
                    val start = i
                    while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) i++
                    val word = line.substring(start, i)
                    if (word in keywords) {
                        builder.pushStyle(SpanStyle(color = keywordColor))
                    }
                    builder.append(word)
                    if (word in keywords) builder.pop()
                    continue
                }
                if (bracketPair != null && (absolute == bracketPair.cursor || absolute == bracketPair.match)) {
                    builder.pushStyle(
                        SpanStyle(
                            background = Color(0x664F72FF),
                            color = Color.Unspecified,
                        ),
                    )
                    builder.append(ch)
                    builder.pop()
                } else {
                    builder.append(ch)
                }
                i++
            }
            if (lineIndex < source.count { it == '\n' }) builder.append('\n')
            global += line.length + 1
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
