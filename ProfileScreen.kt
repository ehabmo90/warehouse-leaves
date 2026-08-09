package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Employee
import com.example.data.models.Leave
import com.example.data.models.PermissionDefs
import com.example.data.models.User
import com.example.ui.components.LeaveCard
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    currentUser: User?,
    employees: List<Employee>,
    leaves: List<Leave>,
    onUpdateInfo: (name: String, phone: String) -> Unit,
    onChangePassword: (oldP: String, newP: String, confP: String) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emp = employees.find { it.id == currentUser?.staffId }
    val userLeaves = leaves.filter { it.empId == currentUser?.staffId }

    var nameInput by remember { mutableStateOf(currentUser?.name ?: "") }
    var phoneInput by remember { mutableStateOf(currentUser?.phone ?: "") }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }

    // Password strength calculation
    val passStrength = remember(newPass) {
        var score = 0
        if (newPass.length >= 6) score++
        if (newPass.length >= 10) score++
        if (newPass.any { it.isUpperCase() } || newPass.any { it.isLowerCase() }) score++
        if (newPass.any { it.isDigit() } || newPass.any { !it.isLetterOrDigit() }) score++
        score
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("👤 حسابي والبيانات الشخصية", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Profile Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryPurpleLight)),
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.take(2) ?: "؟",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.name ?: "المستخدم",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = if (currentUser?.role == "admin") "🛡 مدير النظام" else "👤 موظف",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("📧 ${currentUser?.email}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                Text("📱 ${currentUser?.phone?.ifBlank { "لم يتم إدخال رقم" }}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        // Balances Section if employee
        if (emp != null) {
            val annualRem = emp.annual - emp.consumedAnnual
            val mncRem = Math.max(0, 2 - emp.consumedAllowance)
            val cntDays = String.format("%.1f", Math.max(0, emp.customContainer - emp.consumedContainer) / 540f)

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 أرصدتي الحالية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileBalanceBox("سنوي", "$annualRem يوم", AccentGreen, Modifier.weight(1f))
                        ProfileBalanceBox("منحة", "$mncRem يوم", PrimaryPurple, Modifier.weight(1f))
                        ProfileBalanceBox("وعاء", "$cntDays يوم", PrimaryPurpleLight, Modifier.weight(1f))
                    }
                }
            }
        }

        // Update Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("✏️ تعديل البيانات الشخصية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("الاسم الظاهر", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("رقم الواتساب", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onUpdateInfo(nameInput, phoneInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ التغييرات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Password Change Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🔐 تغيير كلمة المرور", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("كلمة المرور الحالية", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("كلمة المرور الجديدة", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confPass,
                    onValueChange = { confPass = it },
                    label = { Text("تأكيد كلمة المرور الجديدة", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Strength meter
                if (newPass.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val colors = listOf(DangerRed, WarningGold, AccentGreen, PrimaryPurple)
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (i <= passStrength) colors.getOrElse(passStrength - 1) { PrimaryPurple } else SurfaceCard2)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val success = onChangePassword(oldPass, newPass, confPass)
                        if (success) {
                            oldPass = ""
                            newPass = ""
                            confPass = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRedContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تحديث كلمة المرور", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Permissions Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🛡️ صلاحيات حسابك الحالي", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                val permsMap = currentUser?.perms ?: emptyMap()
                PermissionDefs.list.forEach { def ->
                    val level = permsMap[def.key] ?: 0
                    val (statusText, statusColor) = when (level) {
                        2 -> "✏️ كتابة" to AccentGreen
                        1 -> "👁 قراءة" to InfoCyan
                        else -> "🚫 محظور" to TextMuted
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard2)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(def.label, fontSize = 12.sp, color = TextPrimary)
                        Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
            }
        }

        // User's Recent Leaves
        if (userLeaves.isNotEmpty()) {
            Text("📋 آخر إجازاتي", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            userLeaves.take(5).forEach { leave ->
                LeaveCard(leave = leave, isAdmin = false)
            }
        }
    }
}

@Composable
private fun ProfileBalanceBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard2)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, color = TextMuted)
        }
    }
}
