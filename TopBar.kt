package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.NotificationItem
import com.example.data.models.User
import com.example.ui.theme.*

@Composable
fun AppTopBar(
    currentUser: User?,
    notifications: List<NotificationItem>,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNotiPanel by remember { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.read && (it.targetRole == currentUser?.role || it.targetRole == "all") }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = SurfaceCard1.copy(alpha = 0.95f),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, PrimaryPurpleLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏭", fontSize = 25.sp)
                    }

                    Column {
                        Text(
                            text = "Leaves",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "إجازات المخازن",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Right Action Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notifications Icon
                    Box {
                        IconButton(
                            onClick = { showNotiPanel = !showNotiPanel },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard2)
                                .border(1.dp, BorderSubtle)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = if (unreadCount > 0) WarningGold else TextSecondary,
                                modifier = Modifier.size(27.dp)
                            )
                        }

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                                    .border(1.dp, DarkBg, CircleShape)
                            )
                        }
                    }

                    // Dashboard Link
                    IconButton(
                        onClick = { onNavigate("upcoming") },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InfoCyanContainer)
                            .border(1.dp, InfoCyan.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "لوحة الإجازات",
                            tint = InfoCyan,
                            modifier = Modifier.size(27.dp)
                        )
                    }

                    // Profile Link
                    IconButton(
                        onClick = { onNavigate("profile") },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGreenContainer)
                            .border(1.dp, AccentGreen.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "حسابي",
                            tint = AccentGreen,
                            modifier = Modifier.size(27.dp)
                        )
                    }

                    // Admin Panel Link (if admin)
                    if (currentUser?.role == "admin") {
                        IconButton(
                            onClick = { onNavigate("admin") },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryPurple.copy(alpha = 0.2f))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "إدارة",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRedContainer)
                            .border(1.dp, DangerRed.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "تسجيل الخروج",
                            tint = DangerRed,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
            }
        }

        // Notification Panel Dropdown
        AnimatedVisibility(
            visible = showNotiPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = SurfaceCard2,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .border(1.dp, BorderMedium, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔 الإشعارات",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "إغلاق",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showNotiPanel = false }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BorderSubtle
                    )

                    val filteredNotis = notifications.filter {
                        it.targetRole == currentUser?.role || it.targetRole == "all"
                    }

                    if (filteredNotis.isEmpty()) {
                        Text(
                            text = "🔕 لا توجد إشعارات حالياً",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredNotis) { noti ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!noti.read) SurfaceCard3 else SurfaceCard1
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (!noti.read) PrimaryPurple else Color.Transparent)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = noti.text,
                                                color = TextPrimary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = noti.time,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
