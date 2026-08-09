package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Leave
import com.example.ui.theme.*

@Composable
fun LeaveCard(
    leave: Leave,
    isAdmin: Boolean = false,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when (leave.status) {
        "approved" -> AccentGreen
        "rejected" -> DangerRed
        else -> WarningGold
    }

    val statusText = when (leave.status) {
        "approved" -> "✓ مقبول"
        "rejected" -> "✕ مرفوض"
        else -> "⏳ معلق"
    }

    val statusBg = when (leave.status) {
        "approved" -> AccentGreenContainer
        "rejected" -> DangerRedContainer
        else -> WarningGoldContainer
    }

    val statusFg = when (leave.status) {
        "approved" -> AccentGreen
        "rejected" -> DangerRed
        else -> WarningGold
    }

    val typeLabel = when (leave.ltype) {
        "annual" -> "سنوي ثابت"
        "allowance" -> "منحة شهرية"
        else -> "وعاء إضافي"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Right status accent stripe
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(borderColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = leave.empName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$typeLabel · ${leave.days} يوم",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (leave.halfDay) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryPurple.copy(alpha = 0.2f))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "½ يوم",
                                    color = PrimaryPurpleLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .border(1.dp, statusFg.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusFg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Info Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoBox(
                        label = "من",
                        value = leave.from,
                        modifier = Modifier.weight(1f)
                    )
                    InfoBox(
                        label = "إلى",
                        value = leave.to,
                        modifier = Modifier.weight(1f)
                    )
                    InfoBox(
                        label = "المدة",
                        value = "${leave.days} يوم",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (leave.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard2)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "📝 ${leave.notes}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (!leave.rejectReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRedContainer)
                            .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "❌ سبب الرفض: ${leave.rejectReason}",
                            fontSize = 11.sp,
                            color = DangerRed
                        )
                    }
                }

                // Action Buttons for Admin
                if (isAdmin && leave.status == "pending" && onApprove != null && onReject != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قبول", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRedContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رفض", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                        }
                    }
                }

                if (isAdmin && (onEdit != null || onDelete != null)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (onEdit != null) {
                            TextButton(onClick = onEdit) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = InfoCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل", fontSize = 11.sp, color = InfoCyan)
                            }
                        }
                        if (onDelete != null) {
                            TextButton(onClick = onDelete) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف", fontSize = 11.sp, color = DangerRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard2)
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
