package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.HouseholdMemberDto
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.Green50Light
import com.vodang.greenmind.theme.Gray700Dark
import com.vodang.greenmind.theme.Gray400Neutral
import com.vodang.greenmind.theme.Red600Alert

private val green700 = Green700
private val green50 = Green50Light
private val gray700 = Gray700Dark
private val gray400 = Gray400Neutral
private val red600  = Red600Alert

@Composable
internal fun MemberRow(
    member: HouseholdMemberDto,
    isRemoving: Boolean,
    onRemove: () -> Unit,
) {
    val s = LocalAppStrings.current

    val initials = member.fullName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { member.username.take(2).uppercase() }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(green50),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = green700)
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    member.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gray700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "@${member.username}",
                    fontSize = 12.sp,
                    color = gray400
                )
                Text(
                    member.email,
                    fontSize = 11.sp,
                    color = gray400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Role badge
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(green50)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    member.role.replaceFirstChar { it.uppercaseChar() },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = green700
                )
            }

            // Remove button
            if (isRemoving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = red600
                )
            } else {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(s.removeMember, fontSize = 14.sp, color = red600)
                }
            }
        }
    }
}