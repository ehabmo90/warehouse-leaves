package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppBottomNavBar(
    selectedScreen: String,
    pendingBadgeCount: Int,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceCard1.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                title = "الرئيسية",
                icon = Icons.Default.Home,
                isSelected = selectedScreen == "dash",
                onClick = { onNavigate("dash") }
            )

            NavTabItem(
                title = "طلب إجازة",
                icon = Icons.Default.AddCircle,
                isSelected = selectedScreen == "entry",
                badgeCount = pendingBadgeCount,
                onClick = { onNavigate("entry") }
            )

            NavTabItem(
                title = "سجلاتي",
                icon = Icons.Default.ListAlt,
                isSelected = selectedScreen == "records",
                onClick = { onNavigate("records") }
            )

            NavTabItem(
                title = "الأرصدة",
                icon = Icons.Default.PieChart,
                isSelected = selectedScreen == "balance",
                onClick = { onNavigate("balance") }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.6f, label = "alpha")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) PrimaryPurple else TextMuted,
                    modifier = Modifier.size(24.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(DangerRed)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryPurple else TextMuted
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(16.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryPurple)
                )
            }
        }
    }
}
