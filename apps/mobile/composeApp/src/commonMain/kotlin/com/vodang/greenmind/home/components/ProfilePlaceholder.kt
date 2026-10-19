package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green100 = Color(0xFFC8E6C9)

@Composable
fun ProfilePlaceholder(
    user: UserDto? = null,
    onEditClick: () -> Unit = {},
    compact: Boolean = false,
) {
    val s = LocalAppStrings.current

    val avatarSize  = if (compact) 48.dp  else 96.dp
    val avatarFont  = if (compact) 24.sp  else 48.sp
    val nameFont    = if (compact) 14.sp  else 20.sp
    val emailFont   = if (compact) 11.sp  else 14.sp
    val topSpacer   = if (compact) 8.dp   else 32.dp
    val midSpacer   = if (compact) 8.dp   else 16.dp
    val botSpacer   = if (compact) 8.dp   else 32.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (compact) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(topSpacer))
        Box(
            modifier = Modifier
                .size(avatarSize)
                .background(green100, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(avatarSize * 0.6f), tint = Color.White)
        }
        Spacer(Modifier.height(midSpacer))
        Text(
            text = user?.fullName ?: s.profileName,
            fontSize = nameFont,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user?.email ?: s.profileEmail,
            fontSize = emailFont,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(botSpacer))
        SectionCard(modifier = Modifier.clickable { onEditClick() }) {
            Text(s.editProfile, fontWeight = FontWeight.Medium)
        }
    }
}
