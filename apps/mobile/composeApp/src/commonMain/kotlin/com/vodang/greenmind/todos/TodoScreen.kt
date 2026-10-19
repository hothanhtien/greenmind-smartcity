package com.vodang.greenmind.todos

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.TodoItem
import com.vodang.greenmind.store.TodoStore
import com.vodang.greenmind.store.countAll
import com.vodang.greenmind.store.countDone
import com.vodang.greenmind.theme.Green600
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.SurfaceGray as SurfaceGrayTheme
import com.vodang.greenmind.theme.Red500
import com.vodang.greenmind.theme.TextSecondary

private val SurfaceGrayColor = SurfaceGrayTheme

@Composable
fun TodoScreen(
    scrollState: ScrollState? = null,
) {
    val s = LocalAppStrings.current
    val todos by TodoStore.todos.collectAsState()
    val isLoading by TodoStore.isLoading.collectAsState()
    val error by TodoStore.error.collectAsState()
    val generatingIds by TodoStore.generatingIds.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { TodoStore.load() }

    val total = todos.sumOf { it.countAll() }
    val done  = todos.sumOf { it.countDone() }
    val progress = if (total == 0) 0f else done.toFloat() / total

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceGrayColor)
        ) {
            // ── Top spacing ──────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))

            // ── Progress Header Card ──────────────────────────────────────────────
            if (total > 0) {
                ProgressHeaderCard(total = total, done = done, progress = progress)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Error banner ──────────────────────────────────────────────────────
            error?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(msg, color = Red500, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                        IconButton(onClick = { TodoStore.clearError() }) {
                            Icon(Icons.Filled.Close, contentDescription = s.dismissError, tint = Red500)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── List ─────────────────────────────────────────────────────────────
            if (isLoading && todos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green600)
                }
            } else if (todos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Text(
                            s.todosEmpty,
                            color = Color(0xFF9E9E9E),
                            fontSize = 16.sp
                        )
                        Text(
                            "Tap + to add your first task",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                val listScroll = scrollState ?: rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(listScroll)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    todos.forEach { todo ->
                        TodoRootCard(
                            item = todo,
                            generatingIds = generatingIds
                        )
                    }
                }
            }
        }

        // ── Floating Action Button ─────────────────────────────────────────────────
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Green600,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(s.addTodo, fontWeight = FontWeight.Medium)
        }
    }

    // ── Add Todo Dialog ─────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { text ->
                TodoStore.addItem(null, text.trim())
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProgressHeaderCard(total: Int, done: Int, progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular progress
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Green600,
                    trackColor = Green50,
                    strokeWidth = 5.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
            }

            // Stats
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Today's Progress",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "$done / $total tasks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Green600,
                    trackColor = Green50,
                )
            }
        }
    }
}

@Composable
private fun AddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val s = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = s.addTodo,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(s.todosAddPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text(s.addTodo, color = if (text.isNotBlank()) Green600 else Color(0xFFBDBDBD))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel, color = Color(0xFF757575))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TodoRootCard(
    item: TodoItem,
    generatingIds: Set<String>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        TodoItemContent(
            item = item,
            depth = 0,
            generatingIds = generatingIds
        )
    }
}

@Composable
private fun TodoItemContent(
    item: TodoItem,
    depth: Int,
    generatingIds: Set<String>,
) {
    val s = LocalAppStrings.current
    val isGenerating = item.id in generatingIds
    var expanded by remember(item.id) { mutableStateOf(false) }

    var wasGenerating by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(isGenerating) {
        if (wasGenerating && !isGenerating && item.children.isNotEmpty()) expanded = true
        wasGenerating = isGenerating
    }

    val indentStart = (depth * 20).dp
    val hasChildren = item.children.isNotEmpty()

    Column(modifier = Modifier.padding(start = indentStart)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.children.isNotEmpty()) expanded = !expanded
                }
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = item.done,
                onCheckedChange = { TodoStore.toggleItem(item.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Green600,
                    uncheckedColor = Color(0xFFBDBDBD),
                    checkmarkColor = Color.White
                )
            )

            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                fontSize = if (depth == 0) 15.sp else 13.sp,
                fontWeight = if (depth == 0) FontWeight.Medium else FontWeight.Normal,
                color = if (item.done) Color(0xFF9E9E9E) else Color(0xFF212121),
                textDecoration = if (item.done) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF7B1FA2),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = { TodoStore.generateSubtasks(item.id, item.title) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(s.aiWand, fontSize = 16.sp)
                }
            }

            if (hasChildren) {
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = if (expanded) Green600 else Color(0xFFBDBDBD),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(
                onClick = { TodoStore.deleteItem(item.id) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = s.deleteTodo,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (hasChildren && expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, bottom = 8.dp)
            ) {
                HorizontalDivider(
                    color = Green50,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                item.children.forEach { child ->
                    TodoItemContent(
                        item = child,
                        depth = depth + 1,
                        generatingIds = generatingIds
                    )
                }
            }
        }
    }
}
