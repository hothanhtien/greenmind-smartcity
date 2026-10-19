package com.vodang.greenmind.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

/**
 * App bar with green aesthetic:
 * transparent when at top, white + shadow when scrolled.
 *
 * @param title       Primary title text.
 * @param subtitle    Optional smaller text below title (empty = hidden).
 * @param onBack      If non-null, a back arrow circle button is shown on the left.
 * @param scrolled    Drive the transparent→white elevation transition.
 * @param trailing    Optional composable slot for right-side action buttons.
 */
@Composable
fun GreenAppBar(
    title: String,
    subtitle: String = "",
    onBack: (() -> Unit)? = null,
    scrolled: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val s = LocalAppStrings.current
    val bgColor by animateColorAsState(
        targetValue = if (scrolled) Color.White else Color.Transparent,
        animationSpec = tween(200),
        label = "appBarBg"
    )
    val elevation = if (scrolled) 4.dp else 0.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation)
            .background(bgColor)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(green50)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Text(s.backArrow, fontSize = 18.sp, color = green800, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = green800,
            )
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        trailing()
    }
}
