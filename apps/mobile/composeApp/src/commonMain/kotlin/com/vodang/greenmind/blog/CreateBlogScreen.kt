package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.CreateBlogRequest
import com.vodang.greenmind.api.blog.createBlog
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

private val green800  = Color(0xFF2E7D32)
private val green100  = Color(0xFFC8E6C9)
private val divider   = Color(0xFFE4E6EB)
private val gray400   = Color(0xFF9CA3AF)

@Composable
fun CreateBlogScreen(
    accessToken: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val user = SettingsStore.getUser()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsRaw by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val initials = (user?.fullName ?: "")
        .split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("").ifBlank { (user?.username ?: "?").take(2).uppercase() }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // -- Author row ----------------------------------------------------
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Text(
                user?.fullName ?: user?.username ?: "",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF050505),
            )
        }

        HorizontalDivider(thickness = 1.dp, color = divider)

        // -- Form fields ---------------------------------------------------
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 500) title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.blogCreateTitleHint, fontSize = 16.sp, color = gray400) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = green800,
                    unfocusedBorderColor = divider,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                supportingText = { Text("${title.length}/500", fontSize = 11.sp, color = Color.Gray) },
            )

            // Content (large area like FB's "What's on your mind?")
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                placeholder = { Text(s.blogCreateContentHint, fontSize = 15.sp, color = gray400) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = green800,
                    unfocusedBorderColor = divider,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                minLines = 6,
            )

            // Tags
            OutlinedTextField(
                value = tagsRaw,
                onValueChange = { tagsRaw = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.blogCreateTagsHint, fontSize = 14.sp, color = gray400) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Text("#", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green800) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = green800,
                    unfocusedBorderColor = divider,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            // Error
            if (error != null) {
                Text(error!!, fontSize = 13.sp, color = Color(0xFFC62828))
            }
        }

        // -- Bottom Post bar ---------------------------------------------
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = {
                        val trimTitle = title.trim()
                        val trimContent = content.trim()
                        if (trimTitle.isEmpty() || trimContent.isEmpty()) {
                            error = s.blogCreateErrorEmpty
                            return@Button
                        }
                        val tags = tagsRaw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                        scope.launch {
                            isSubmitting = true; error = null
                            try {
                                createBlog(accessToken, CreateBlogRequest(trimTitle, trimContent, tags))
                                onCreated()
                            } catch (e: Throwable) {
                                error = e.message ?: s.blogErrorLoad
                            }
                            isSubmitting = false
                        }
                    },
                    enabled = !isSubmitting && title.isNotBlank() && content.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green800,
                        disabledContainerColor = green800.copy(alpha = 0.4f),
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        if (isSubmitting) s.blogCreateSubmitting else s.blogCreateSubmit,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
