package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import com.example.data.models.Employee
import com.example.data.models.Leave
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingLeavesScreen(
    leaves: List<Leave>,
    employees: List<Employee>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayStr = sdf.format(Date())

    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("dynamic") } // "dynamic", "all", "today", "week", "month"
    var selectedEmpId by remember { mutableStateOf<Int?>(null) }

    var expandedPeriodMenu by remember { mutableStateOf(false) }
    var expandedEmpMenu by remember { mutableStateOf(false) }

    val approvedLeaves = remember(leaves) {
        leaves.filter { it.status == "approved" }
    }

    val filteredLeaves = remember(approvedLeaves, searchQuery, selectedPeriod, selectedEmpId) {
        val cal = Calendar.getInstance()
        val oneMonthLaterCal = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }
        val oneMonthLaterStr = sdf.format(oneMonthLaterCal.time)

        val sevenDaysLaterCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
        val sevenDaysLaterStr = sdf.format(sevenDaysLaterCal.time)

        approvedLeaves.filter { l ->
            val matchEmp = selectedEmpId == null || l.empId == selectedEmpId
            val matchSearch = searchQuery.isBlank() || l.empName.contains(searchQuery, ignoreCase = true)

            val matchPeriod = when (selectedPeriod) {
                "dynamic" -> l.to >= todayStr && l.from <= oneMonthLaterStr
                "today" -> l.from <= todayStr && l.to >= todayStr
                "week" -> l.from <= sevenDaysLaterStr && l.to >= todayStr
                "month" -> l.from <= oneMonthLaterStr && l.to >= todayStr
                else -> l.to >= todayStr
            }

            matchEmp && matchSearch && matchPeriod
        }.sortedBy { it.from }
    }

    // Calculations for summary stats
    val countToday = approvedLeaves.count { it.from <= todayStr && it.to >= todayStr }
    val sevenDaysStr = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }.time)
    val countWeek = approvedLeaves.count { it.from <= sevenDaysStr && it.to >= todayStr }
    val oneMonthStr = sdf.format(Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time)
    val countMonth = approvedLeaves.count { it.from <= oneMonthStr && it.to >= todayStr }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("لوحة الإجازات القادمة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryPurpleLight)),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("📋 الإجازات القادمة", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("جميع إجازات الموظفين المقبولة - من اليوم فصاعداً", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📅 ${filteredLeaves.size} إجازة قادمة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Summary Boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryBox("إجازة اليوم", countToday.toString(), DangerRed, Modifier.weight(1f))
            SummaryBox("خلال أسبوع", countWeek.toString(), WarningGold, Modifier.weight(1f))
            SummaryBox("خلال شهر", countMonth.toString(), AccentGreen, Modifier.weight(1f))
        }

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("🔍 بحث بالاسم...", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard1,
                    unfocusedContainerColor = SurfaceCard1,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.weight(1f)
            )

            // Period Selector
            ExposedDropdownMenuBox(
                expanded = expandedPeriodMenu,
                onExpandedChange = { expandedPeriodMenu = !expandedPeriodMenu }
            ) {
                val periodLabel = when (selectedPeriod) {
                    "dynamic" -> "شهر من اليوم"
                    "today" -> "اليوم فقط"
                    "week" -> "خلال أسبوع"
                    "month" -> "خلال شهر"
                    else -> "الكل"
                }

                OutlinedTextField(
                    value = periodLabel,
                    onValueChange = {},
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard1,
                        unfocusedContainerColor = SurfaceCard1,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedPeriodMenu,
                    onDismissRequest = { expandedPeriodMenu = false },
                    modifier = Modifier.background(SurfaceCard2)
                ) {
                    DropdownMenuItem(
                        text = { Text("شهر من اليوم 📅", color = TextPrimary) },
                        onClick = { selectedPeriod = "dynamic"; expandedPeriodMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("كل الإجازات القادمة", color = TextPrimary) },
                        onClick = { selectedPeriod = "all"; expandedPeriodMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("اليوم فقط", color = DangerRed) },
                        onClick = { selectedPeriod = "today"; expandedPeriodMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("خلال 7 أيام", color = WarningGold) },
                        onClick = { selectedPeriod = "week"; expandedPeriodMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("خلال 30 يوم", color = AccentGreen) },
                        onClick = { selectedPeriod = "month"; expandedPeriodMenu = false }
                    )
                }
            }
        }

        // Table List
        if (filteredLeaves.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("📅 لا توجد إجازات مقبولة في الفترة المحددة", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(filteredLeaves) { index, leave ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.width(28.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = leave.empName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "من ${leave.from} إلى ${leave.to}",
                                    fontSize = 11.sp,
                                    color = InfoCyan
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentGreenContainer)
                                    .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${leave.days} يوم",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }
    }
}
