package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Employee
import com.example.data.models.Leave
import com.example.data.models.User
import com.example.ui.components.CalendarStrip
import com.example.ui.components.LeaveCard
import com.example.ui.components.StatGrid
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    currentUser: User?,
    employees: List<Employee>,
    leaves: List<Leave>,
    onNavigate: (String) -> Unit,
    onApproveLeave: (Leave) -> Unit,
    onRejectLeave: (Leave) -> Unit,
    modifier: Modifier = Modifier
) {
    val emp = employees.find { it.id == currentUser?.staffId }
    val pendingLeaves = leaves.filter { it.status == "pending" }
    val approvedLeaves = leaves.filter { it.status == "approved" }
    val upcomingCount = leaves.count { it.status == "approved" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Greeting Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryPurple, PrimaryPurpleLight)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "مرحباً بك 👋", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Text(
                        text = currentUser?.name ?: "المستخدم",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (currentUser?.role == "admin") "🛡 مدير النظام" else "👤 موظف",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    if (emp != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val annualRem = emp.annual - emp.consumedAnnual
                            val mncRem = Math.max(0, 2 - emp.consumedAllowance)
                            val cntDays = String.format("%.1f", Math.max(0, emp.customContainer - emp.consumedContainer) / 540f)

                            BalanceChip("سنوي", "$annualRem يوم", Modifier.weight(1f))
                            BalanceChip("منحة", "$mncRem يوم", Modifier.weight(1f))
                            BalanceChip("وعاء", "$cntDays يوم", Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Stat Grid
        item {
            StatGrid(
                pendingCount = pendingLeaves.size,
                approvedCount = approvedLeaves.size,
                staffCount = employees.size,
                upcomingCount = upcomingCount
            )
        }

        // Calendar Strip
        item {
            Column {
                Text(
                    text = "🗓️ الأيام المحجوزة (14 يوم قادم)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                CalendarStrip(leaves = leaves)
            }
        }

        // Recent Pending Requests
        item {
            Text(
                text = "⏳ آخر الإجازات المعلقة",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (pendingLeaves.isEmpty()) {
            item {
                EmptyBox("لا توجد طلبات معلقة حالياً")
            }
        } else {
            items(pendingLeaves.take(4)) { leave ->
                LeaveCard(
                    leave = leave,
                    isAdmin = currentUser?.role == "admin",
                    onApprove = { onApproveLeave(leave) },
                    onReject = { onRejectLeave(leave) }
                )
            }
        }

        // Recent Approved Requests
        item {
            Text(
                text = "✅ آخر الإجازات المقبولة",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (approvedLeaves.isEmpty()) {
            item {
                EmptyBox("لا توجد إجازات مقبولة مؤخراً")
            }
        } else {
            items(approvedLeaves.take(4)) { leave ->
                LeaveCard(leave = leave, isAdmin = false)
            }
        }

        // Upcoming Dashboard Banner Button
        item {
            Button(
                onClick = { onNavigate("upcoming") },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard2),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فتح لوحة الإجازات القادمة (للجميع)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BalanceChip(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun EmptyBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextMuted, fontSize = 12.sp)
    }
}
