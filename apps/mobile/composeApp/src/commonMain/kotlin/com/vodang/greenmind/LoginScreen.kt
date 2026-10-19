package com.vodang.greenmind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.vodang.greenmind.accounts.Account
import com.vodang.greenmind.accounts.AccountsRepository
import com.vodang.greenmind.api.auth.LoginEmailRequest
import com.vodang.greenmind.api.auth.loginWithEmail
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    val s = LocalAppStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val green800 = Color(0xFF2E7D32)
    val green500 = Color(0xFF4CAF50)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9))))
    ) {
        val maxH = maxHeight
        val isSmall = maxH < 600.dp

        val logoSize = if (isSmall) 64.dp else 88.dp
        val logoEmoji = if (isSmall) 28.sp else 40.sp
        val titleFont = if (isSmall) 20.sp else 30.sp
        val cardPadding = if (isSmall) 16.dp else 20.dp
        val fieldSpacing = if (isSmall) 10.dp else 12.dp
        val topSpacer = if (isSmall) 16.dp else 32.dp
        val bottomPadding = if (isSmall) 12.dp else 20.dp

        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        var showAccountsDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }
        var addEmail by rememberSaveable { mutableStateOf("") }
        var addPassword by rememberSaveable { mutableStateOf("") }

        val accounts by AccountsRepository.accountsFlow.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(topSpacer))

            // Logo + title
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .background(green800, shape = CircleShape)
                    .clickable { showAccountsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", fontSize = logoEmoji)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.appName,
                fontSize = titleFont,
                fontWeight = FontWeight.Bold,
                color = green800,
            )
            Text(
                text = s.appSubtitle,
                fontSize = if (isSmall) 11.sp else 12.sp,
                color = Color(0xFF66BB6A),
            )

            Spacer(Modifier.height(if (isSmall) 12.dp else 20.dp))

            // Form card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(cardPadding).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(fieldSpacing),
                ) {
                    Text(
                        text = s.signIn,
                        fontSize = if (isSmall) 17.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(s.emailAddress) },
                        placeholder = { Text(s.emailPlaceholder) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800,
                        ),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(s.password) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                Text(
                                    if (passwordVisible) s.hide else s.show,
                                    fontSize = 12.sp,
                                    color = green500,
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800,
                        ),
                    )

                    // Remember + forgot row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = green800,
                                checkmarkColor = Color.White,
                                uncheckedColor = Color(0xFF9E9E9E),
                            ),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            s.rememberMe,
                            fontSize = 13.sp,
                            color = Color(0xFF424242),
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { }) {
                            Text(
                                s.forgotPassword,
                                fontSize = 12.sp,
                                color = green500,
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            errorMessage = null
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = s.loginError
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    val resp = loginWithEmail(
                                        LoginEmailRequest(email = email.trim(), password = password),
                                    )
                                    SettingsStore.setAccessToken(resp.accessToken)
                                    SettingsStore.setRefreshToken(resp.refreshToken)
                                    SettingsStore.setUser(resp.user)
                                    onLoginSuccess()
                                } catch (t: Throwable) {
                                    errorMessage = t.message ?: "Login failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(if (isSmall) 46.dp else 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(s.loggingIn, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                s.loginButton,
                                fontSize = if (isSmall) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(if (isSmall) 12.dp else 16.dp))

            // Divider + Google button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                    Text(s.or, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFBDBDBD))
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSmall) 46.dp else 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDBDBDB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3C4043)),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                        Spacer(Modifier.width(10.dp))
                        Text(s.continueWithGoogle, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(if (isSmall) 8.dp else 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.noAccount, fontSize = 13.sp, color = Color(0xFF757575))
                    TextButton(onClick = { onNavigateToRegister() }) {
                        Text(s.signUp, fontSize = 13.sp, color = green800, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(bottomPadding))
        }

        // ── Account picker dialog ──────────────────────────────────────────────
        if (showAccountsDialog) {
            AlertDialog(
                onDismissRequest = { showAccountsDialog = false },
                title = { Text(s.chooseAccount) },
                text = {
                    Column {
                        if (accounts.isEmpty()) Text(s.noSavedAccounts)
                        accounts.forEach { a ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(a.email, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                TextButton(onClick = {
                                    email = a.email
                                    password = a.password
                                    showAccountsDialog = false
                                }) { Text(s.select) }
                                TextButton(onClick = { scope.launch { AccountsRepository.removeAccount(a) } }) { Text(s.delete) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { showAddDialog = true }) { Text(s.addAccount) }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showAccountsDialog = false }) { Text(s.close) } },
            )
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(s.addAccount) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = addEmail,
                            onValueChange = { addEmail = it },
                            label = { Text(s.emailAddress) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = addPassword,
                            onValueChange = { addPassword = it },
                            label = { Text(s.password) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val a = Account(addEmail.trim(), addPassword)
                        scope.launch { AccountsRepository.addAccount(a) }
                        email = a.email
                        password = a.password
                        addEmail = ""
                        addPassword = ""
                        showAddDialog = false
                        showAccountsDialog = false
                    }) { Text(s.save) }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text(s.cancel) } },
            )
        }
    }
}
