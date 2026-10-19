package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.addMemberToHousehold
import com.vodang.greenmind.api.households.removeMemberFromHousehold
import com.vodang.greenmind.api.households.HouseholdDto
import com.vodang.greenmind.qr.QrCodeScannerScreen
import com.vodang.greenmind.householdwaste.components.MemberRow
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.Gray700Dark
import com.vodang.greenmind.theme.Gray400Neutral
import com.vodang.greenmind.theme.Red600Alert
import com.vodang.greenmind.theme.Red50Light
import com.vodang.greenmind.theme.SurfaceGray
import kotlinx.coroutines.launch

private val green700  = Green700
private val gray700  = Gray700Dark
private val gray400  = Gray400Neutral
private val red600   = Red600Alert
private val red50    = Red50Light

@Composable
fun HouseholdSettingsScreen(onBack: () -> Unit) {
    val s = LocalAppStrings.current
    val household by HouseholdStore.household.collectAsState()
    val h = household ?: run { onBack(); return }
    val token = SettingsStore.getAccessToken() ?: run { onBack(); return }

    val scope = rememberCoroutineScope()

    var addUserId     by remember { mutableStateOf("") }
    var isAdding      by remember { mutableStateOf(false) }
    var addError      by remember { mutableStateOf<String?>(null) }
    var addSuccess    by remember { mutableStateOf(false) }
    var removingId    by remember { mutableStateOf<String?>(null) }
    var removeError   by remember { mutableStateOf<String?>(null) }
    var confirmRemoveMemberId by remember { mutableStateOf<String?>(null) }

    var editingAddress by remember { mutableStateOf(false) }
    var editAddressText by remember { mutableStateOf(h.address) }
    var isSavingAddress by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var addressSuccess by remember { mutableStateOf(false) }

    var showQrScanner by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var isDeletingHousehold by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    var members by remember(h) { mutableStateOf(h.members ?: emptyList()) }

    // ── Confirm remove member dialog ───────────────────────────────────────────
    confirmRemoveMemberId?.let { memberId ->
        AlertDialog(
            onDismissRequest = { confirmRemoveMemberId = null },
            title = { Text(s.removeMember, fontWeight = FontWeight.Bold) },
            text = { Text(s.confirmRemoveMember) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmRemoveMemberId = null
                        scope.launch {
                            removingId = memberId
                            removeError = null
                            try {
                                removeMemberFromHousehold(token, memberId)
                                members = members.filter { it.id != memberId }
                                HouseholdStore.fetchHousehold()
                            } catch (e: Throwable) {
                                removeError = s.failedToRemoveMember
                            } finally {
                                removingId = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = red600),
                ) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemoveMemberId = null }) { Text(s.cancel) }
            },
        )
    }

    // ── Confirm delete household dialog ────────────────────────────────────────
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(s.deleteHousehold, fontWeight = FontWeight.Bold) },
            text = { Text(s.confirmDeleteHousehold) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        scope.launch {
                            isDeletingHousehold = true
                            deleteError = null
                            try {
                                members.forEach { member ->
                                    runCatching { removeMemberFromHousehold(token, member.id) }
                                }
                                HouseholdStore.clearHousehold()
                                onBack()
                            } catch (e: Throwable) {
                                deleteError = s.failedToDeleteHousehold
                            } finally {
                                isDeletingHousehold = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = red600),
                ) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(s.cancel) }
            },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
            // ── Edit address card ──────────────────────────────────────────────
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = green700)
                        Text(
                            s.editAddress,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = gray700
                        )
                    }
                    OutlinedTextField(
                        value = editAddressText,
                        onValueChange = {
                            editAddressText = it
                            addressError = null
                            addressSuccess = false
                        },
                        label = { Text(s.addressLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = addressError != null,
                        supportingText = when {
                            addressError != null -> {{ Text(addressError!!, color = red600) }}
                            addressSuccess -> {{ Text(s.addressUpdated, color = green700) }}
                            else -> null
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green700,
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                        ),
                    )
                    Button(
                        onClick = {
                            val addr = editAddressText.trim()
                            if (addr.isBlank()) { addressError = s.failedToUpdateAddress; return@Button }
                            scope.launch {
                                isSavingAddress = true
                                addressError = null
                                addressSuccess = false
                                try {
                                    val ok = HouseholdStore.updateHouseholdAddress(
                                        address = addr,
                                        lat = h.lat.toDoubleOrNull() ?: 0.0,
                                        lng = h.lng.toDoubleOrNull() ?: 0.0,
                                    )
                                    if (ok) addressSuccess = true
                                    else addressError = s.failedToUpdateAddress
                                } catch (e: Throwable) {
                                    addressError = s.failedToUpdateAddress
                                } finally {
                                    isSavingAddress = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green700),
                        enabled = !isSavingAddress,
                    ) {
                        if (isSavingAddress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(s.saveAddress)
                    }
                }
            }

            // ── Add member card ───────────────────────────────────────────────
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        s.addMember,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = gray700
                    )
                    OutlinedTextField(
                        value = addUserId,
                        onValueChange = {
                            addUserId = it
                            addError = null
                            addSuccess = false
                        },
                        label = { Text(s.userId) },
                        placeholder = { Text(s.userIdPlaceholder) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = addError != null,
                        supportingText = when {
                            addError != null -> { { Text(addError!!, color = red600) } }
                            addSuccess -> { { Text(s.memberAddedSuccess, color = green700) } }
                            else -> null
                        },
                        trailingIcon = {
                            IconButton(onClick = { showQrScanner = true }) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan QR", tint = green700)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green700,
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                        ),
                    )
                    Button(
                        onClick = {
                            val uid = addUserId.trim()
                            if (uid.isBlank()) { addError = s.userIdCannotBeEmpty; return@Button }
                            scope.launch {
                                isAdding = true
                                addError = null
                                try {
                                    addMemberToHousehold(token, h, uid)
                                    addUserId = ""
                                    addSuccess = true
                                    HouseholdStore.fetchHousehold()
                                } catch (e: Throwable) {
                                    addError = s.failedToAddMember
                                } finally {
                                    isAdding = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green700),
                        enabled = !isAdding,
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(s.addMember)
                    }
                }
            }

            // ── Members list ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    s.members,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = gray700
                )
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5E7EB))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("${members.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                }
            }

            if (removeError != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = red50),
                ) {
                    Text(
                        removeError!!,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = red600
                    )
                }
            }

            if (members.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.noMembersYet, fontSize = 13.sp, color = gray400)
                }
            } else {
                members.forEach { member ->
                    MemberRow(
                        member      = member,
                        isRemoving  = removingId == member.id,
                        onRemove    = { confirmRemoveMemberId = member.id },
                    )
                }
            }

            // ── Delete household ──────────────────────────────────────────────
            HorizontalDivider(color = Color(0xFFE0E0E0))
            if (deleteError != null) {
                Text(deleteError!!, fontSize = 12.sp, color = red600)
            }
            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = red600),
                enabled = !isDeletingHousehold,
            ) {
                if (isDeletingHousehold) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.deleteHousehold)
            }

            Spacer(Modifier.navigationBarsPadding())
        }

        // ── QR Scanner overlay ──────────────────────────────────────────────────
    if (showQrScanner) {
        QrCodeScannerScreen(
            onScanResult = { result ->
                addUserId = result
                showQrScanner = false
            },
            onBack = { showQrScanner = false },
        )
    }
}
