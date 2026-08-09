package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Employee
import com.example.data.models.User
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestTab(
    currentUser: User?,
    employees: List<Employee>,
    allowHalfDay: Boolean,
    onSubmitRequest: (fromDate: String, toDate: String, isHalfDay: Boolean, leaveType: String, notes: String, targetStaffId: Int?, autoApprove: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.role == "admin" || (currentUser?.perms?.get("approve_leaves") ?: 0) >= 2

    var selectedEmpId by remember(currentUser, employees) {
        mutableStateOf(currentUser?.staffId ?: employees.firstOrNull()?.id ?: 1)
    }
    var expandedEmpDropdown by remember { mutableStateOf(false) }
    var autoApprove by remember { mutableStateOf(isAdmin) }

    val activeEmp = employees.find { it.id == selectedEmpId } ?: employees.find { it.id == currentUser?.staffId }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val todayStr = sdf.format(Date())

    var fromDate by remember { mutableStateOf(todayStr) }
    var toDate by remember { mutableStateOf(todayStr) }
    var isHalfDay by remember { mutableStateOf(false) }
    var leaveType by remember { mutableStateOf("annual") }
    var notes by remember { mutableStateOf("") }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    fun showDatePicker(initialDate: String, onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        try {
            sdf.parse(initialDate)?.let { cal.time = it }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                onDateSelected(sdf.format(selectedCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryPurple)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isAdmin) "🛡 طلب / تسجيل إجازة موظف" else "طلب إجازة جديد",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isAdmin) "إمكانية إدخال إجازة لأي موظف بالمنشأة" else "تقديم لـ: ${currentUser?.name ?: "الموظف"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Employee picker for Manager/Admin
                if (isAdmin && employees.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedEmpDropdown,
                        onExpandedChange = { expandedEmpDropdown = !expandedEmpDropdown }
                    ) {
                        OutlinedTextField(
                            value = activeEmp?.name ?: "اختر موظفاً",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اختر الموظف المراد تقديم الإجازة له", color = PrimaryPurpleLight, fontWeight = FontWeight.Bold) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmpDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceCard2,
                                unfocusedContainerColor = SurfaceCard2,
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = PrimaryPurpleLight.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedEmpDropdown,
                            onDismissRequest = { expandedEmpDropdown = false },
                            modifier = Modifier.background(SurfaceCard2)
                        ) {
                            employees.forEach { employee ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = employee.name,
                                            fontWeight = if (employee.id == selectedEmpId) FontWeight.Bold else FontWeight.Normal,
                                            color = if (employee.id == selectedEmpId) PrimaryPurpleLight else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        selectedEmpId = employee.id
                                        expandedEmpDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Balance summary
                if (activeEmp != null) {
                    val annualRem = activeEmp.annual - activeEmp.consumedAnnual
                    val mncRem = Math.max(0, 2 - activeEmp.consumedAllowance)
                    val cntDays = String.format("%.1f", Math.max(0, activeEmp.customContainer - activeEmp.consumedContainer) / 540f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BalanceChip("سنوي", "$annualRem يوم", AccentGreen, Modifier.weight(1f))
                        BalanceChip("منحة", "$mncRem يوم", PrimaryPurple, Modifier.weight(1f))
                        BalanceChip("وعاء", "$cntDays يوم", PrimaryPurpleLight, Modifier.weight(1f))
                    }
                }

                // Dates selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاريخ البداية", color = TextMuted) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.clickable { showDatePicker(fromDate) { fromDate = it } }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard2,
                            unfocusedContainerColor = SurfaceCard2,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker(fromDate) { fromDate = it } }
                    )

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاريخ النهاية", color = TextMuted) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.clickable { showDatePicker(toDate) { toDate = it } }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard2,
                            unfocusedContainerColor = SurfaceCard2,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker(toDate) { toDate = it } }
                    )
                }

                // Half day toggle
                if (allowHalfDay) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard2)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⏱️ نصف يوم فقط", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Switch(
                            checked = isHalfDay,
                            onCheckedChange = { isHalfDay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }
                }

                // Auto Approve toggle for Admin
                if (isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard2)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "⚡ اعتماد الإجازة فوراً", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "خصم الإجازة مباشرة من رصيد الموظف دون انتظار موافقة", fontSize = 10.sp, color = TextMuted)
                        }
                        Switch(
                            checked = autoApprove,
                            onCheckedChange = { autoApprove = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentGreen
                            )
                        )
                    }
                }

                // Leave type selector dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    val currentLabel = when (leaveType) {
                        "annual" -> "الرصيد السنوي الثابت (21 يوم)"
                        "allowance" -> "المنحة الشهرية (2 يوم/شهر)"
                        else -> "الوعاء التراكمي الإضافي"
                    }

                    OutlinedTextField(
                        value = currentLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الرصيد", color = TextMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard2,
                            unfocusedContainerColor = SurfaceCard2,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false },
                        modifier = Modifier.background(SurfaceCard2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("الرصيد السنوي الثابت (21 يوم)", color = TextPrimary) },
                            onClick = { leaveType = "annual"; expandedTypeDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("المنحة الشهرية (2 يوم/شهر)", color = TextPrimary) },
                            onClick = { leaveType = "allowance"; expandedTypeDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("الوعاء التراكمي الإضافي", color = TextPrimary) },
                            onClick = { leaveType = "container"; expandedTypeDropdown = false }
                        )
                    }
                }

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية", color = TextMuted) },
                    placeholder = { Text("أي تفاصيل أخرى...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard2,
                        unfocusedContainerColor = SurfaceCard2,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onSubmitRequest(
                            fromDate,
                            toDate,
                            isHalfDay,
                            leaveType,
                            notes,
                            if (isAdmin) selectedEmpId else currentUser?.staffId,
                            if (isAdmin) autoApprove else false
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAdmin && autoApprove) AccentGreen else PrimaryPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdmin && autoApprove) "تسجيل وإعتماد الإجازة فوراً" else "إرسال الطلب للمراجعة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard2)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }
    }
}
