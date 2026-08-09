package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.LeaveCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    currentUser: User?,
    employees: List<Employee>,
    users: List<User>,
    leaves: List<Leave>,
    settings: AppSettings,
    onApproveLeave: (Leave) -> Unit,
    onRejectLeave: (Leave) -> Unit,
    onDeleteLeave: (Leave) -> Unit,
    onAddEmployee: (String, String, Int) -> Unit,
    onDeleteEmployee: (Int) -> Unit,
    onUpdateContainer: (empId: Int, mins: Int, isAdd: Boolean) -> Unit,
    onUpdateAnnual: (empId: Int, days: Int, isAdd: Boolean) -> Unit,
    onAddUser: (email: String, phone: String, pass: String, staffId: Int?, role: String) -> Unit,
    onDeleteUser: (Long) -> Unit,
    onUpdatePermissions: (userId: Long, Map<String, Int>) -> Unit,
    onUpdateSettings: (AppSettings) -> Unit,
    onResetSystem: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("pending") } // pending, emps, balances, users, perms, settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("⚙️ لوحة التحكم والإدارة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Horizontal Icon Nav Tabs
        ScrollableTabRow(
            selectedTabIndex = when (activeSubTab) {
                "pending" -> 0
                "emps" -> 1
                "balances" -> 2
                "users" -> 3
                "perms" -> 4
                else -> 5
            },
            containerColor = SurfaceCard1,
            contentColor = PrimaryPurple,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            val pendingCount = leaves.count { it.status == "pending" }

            Tab(
                selected = activeSubTab == "pending",
                onClick = { activeSubTab = "pending" },
                text = { Text("الطلبات ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == "emps",
                onClick = { activeSubTab = "emps" },
                text = { Text("الموظفون", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == "balances",
                onClick = { activeSubTab = "balances" },
                text = { Text("الأرصدة", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == "users",
                onClick = { activeSubTab = "users" },
                text = { Text("المستخدمون", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == "perms",
                onClick = { activeSubTab = "perms" },
                text = { Text("الصلاحيات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == "settings",
                onClick = { activeSubTab = "settings" },
                text = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Sub Tab Contents
        when (activeSubTab) {
            "pending" -> PendingTabContent(
                leaves = leaves.filter { it.status == "pending" },
                onApprove = onApproveLeave,
                onReject = onRejectLeave,
                onDelete = onDeleteLeave
            )

            "emps" -> EmployeesTabContent(
                employees = employees,
                onAddEmployee = onAddEmployee,
                onDeleteEmployee = onDeleteEmployee
            )

            "balances" -> BalancesTabContent(
                employees = employees,
                onUpdateContainer = onUpdateContainer,
                onUpdateAnnual = onUpdateAnnual
            )

            "users" -> UsersTabContent(
                users = users,
                employees = employees,
                onAddUser = onAddUser,
                onDeleteUser = onDeleteUser
            )

            "perms" -> PermissionsTabContent(
                users = users,
                onUpdatePermissions = onUpdatePermissions
            )

            "settings" -> SettingsTabContent(
                settings = settings,
                onUpdateSettings = onUpdateSettings,
                onResetSystem = onResetSystem
            )
        }
    }
}

@Composable
private fun PendingTabContent(
    leaves: List<Leave>,
    onApprove: (Leave) -> Unit,
    onReject: (Leave) -> Unit,
    onDelete: (Leave) -> Unit
) {
    if (leaves.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("✅ لا توجد طلبات معلقة حالياً - جميع الطلبات تمت معالجتها", color = TextMuted, fontSize = 13.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(leaves) { leave ->
                LeaveCard(
                    leave = leave,
                    isAdmin = true,
                    onApprove = { onApprove(leave) },
                    onReject = { onReject(leave) },
                    onDelete = { onDelete(leave) }
                )
            }
        }
    }
}

@Composable
private fun EmployeesTabContent(
    employees: List<Employee>,
    onAddEmployee: (String, String, Int) -> Unit,
    onDeleteEmployee: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var annual by remember { mutableStateOf("21") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Employee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("👤 إضافة موظف جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الواتساب (مثال: 201xxxxxxxxx)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = annual,
                    onValueChange = { annual = it },
                    label = { Text("الرصيد السنوي (أيام)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val annualVal = annual.toIntOrNull() ?: 21
                        onAddEmployee(name, phone, annualVal)
                        name = ""
                        phone = ""
                        annual = "21"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة الموظف", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Text("📋 كشف الموظفين الحاليين", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        employees.forEach { emp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(emp.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("📱 ${emp.phone.ifBlank { "بلا رقم" }}", fontSize = 11.sp, color = TextMuted)
                        Text("سنوي: ${emp.annual} يوم", fontSize = 10.sp, color = AccentGreen)
                    }

                    IconButton(onClick = { onDeleteEmployee(emp.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalancesTabContent(
    employees: List<Employee>,
    onUpdateContainer: (empId: Int, mins: Int, isAdd: Boolean) -> Unit,
    onUpdateAnnual: (empId: Int, days: Int, isAdd: Boolean) -> Unit
) {
    var selectedEmpId by remember { mutableStateOf(employees.firstOrNull()?.id ?: 1) }
    var minsInput by remember { mutableStateOf("") }
    var annualInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Container Pool Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⏱️ منح وعاء إضافي (بالدقائق - 540 دقيقة = 1 يوم)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                // Select Employee
                Text("اختر الموظف:", fontSize = 11.sp, color = TextMuted)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    employees.take(4).forEach { e ->
                        FilterChip(
                            selected = selectedEmpId == e.id,
                            onClick = { selectedEmpId = e.id },
                            label = { Text(e.name.substringBefore(" "), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = minsInput,
                    onValueChange = { minsInput = it },
                    label = { Text("القيمة بالدقائق (مثال: 270 = نصف يوم)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val mins = minsInput.toIntOrNull() ?: 0
                            onUpdateContainer(selectedEmpId, mins, false)
                            minsInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تعديل", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val mins = minsInput.toIntOrNull() ?: 0
                            onUpdateContainer(selectedEmpId, mins, true)
                            minsInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة +", color = Color.White)
                    }
                }
            }
        }

        // Annual Days Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("📅 تعديل الرصيد السنوي (أيام)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = annualInput,
                    onValueChange = { annualInput = it },
                    label = { Text("عدد الأيام", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val days = annualInput.toIntOrNull() ?: 0
                            onUpdateAnnual(selectedEmpId, days, false)
                            annualInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تعديل", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val days = annualInput.toIntOrNull() ?: 0
                            onUpdateAnnual(selectedEmpId, days, true)
                            annualInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة +", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersTabContent(
    users: List<User>,
    employees: List<Employee>,
    onAddUser: (email: String, phone: String, pass: String, staffId: Int?, role: String) -> Unit,
    onDeleteUser: (Long) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    var selectedStaffId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("➕ إضافة مستخدم جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الواتساب", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("كلمة المرور الإبتدائية", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == "user",
                        onClick = { selectedRole = "user" },
                        label = { Text("موظف (User)") }
                    )
                    FilterChip(
                        selected = selectedRole == "admin",
                        onClick = { selectedRole = "admin" },
                        label = { Text("مدير (Admin)") }
                    )
                }

                Button(
                    onClick = {
                        onAddUser(email, phone, pass, selectedStaffId, selectedRole)
                        email = ""
                        phone = ""
                        pass = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ المستخدم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("👥 قائمة الحسابات المسجلة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        users.forEach { u ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.name.ifBlank { u.email }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(u.email, fontSize = 11.sp, color = TextMuted)
                        Text(if (u.role == "admin") "🛡 مدير" else "👤 موظف", fontSize = 10.sp, color = PrimaryPurple)
                    }

                    IconButton(onClick = { onDeleteUser(u.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsTabContent(
    users: List<User>,
    onUpdatePermissions: (userId: Long, Map<String, Int>) -> Unit
) {
    var selectedUserId by remember { mutableStateOf(users.firstOrNull()?.id ?: 0L) }
    val selectedUser = users.find { it.id == selectedUserId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🛡️ جدول الصلاحيات التفصيلي للمستخدمين", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        // User Picker Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            users.take(5).forEach { u ->
                FilterChip(
                    selected = selectedUserId == u.id,
                    onClick = { selectedUserId = u.id },
                    label = { Text(u.name.take(6), fontSize = 10.sp) }
                )
            }
        }

        if (selectedUser != null) {
            val perms = selectedUser.perms.toMutableMap()

            PermissionDefs.list.forEach { def ->
                val currentLevel = perms[def.key] ?: 0

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(def.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(def.desc, fontSize = 10.sp, color = TextMuted)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = currentLevel == 0,
                                onClick = {
                                    perms[def.key] = 0
                                    onUpdatePermissions(selectedUser.id, perms)
                                },
                                label = { Text("محظور", fontSize = 9.sp) }
                            )
                            FilterChip(
                                selected = currentLevel == 1,
                                onClick = {
                                    perms[def.key] = 1
                                    onUpdatePermissions(selectedUser.id, perms)
                                },
                                label = { Text("قراءة", fontSize = 9.sp) }
                            )
                            FilterChip(
                                selected = currentLevel == 2,
                                onClick = {
                                    perms[def.key] = 2
                                    onUpdatePermissions(selectedUser.id, perms)
                                },
                                label = { Text("كتابة", fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onResetSystem: () -> Unit
) {
    var systemName by remember { mutableStateOf(settings.systemName) }
    var adminPhone by remember { mutableStateOf(settings.adminPhone) }
    var maxPerDay by remember { mutableStateOf(settings.maxPerDay.toString()) }
    var halfMins by remember { mutableStateOf(settings.halfDayMinutes.toString()) }
    var allowHalf by remember { mutableStateOf(settings.allowHalfDay) }
    var waEnabled by remember { mutableStateOf(settings.waEnabled) }
    var supabaseUrl by remember { mutableStateOf(settings.supabaseUrl) }
    var supabaseKey by remember { mutableStateOf(settings.supabaseAnonKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚙️ إعدادات النظام العامة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                OutlinedTextField(
                    value = systemName,
                    onValueChange = { systemName = it },
                    label = { Text("اسم النظام", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adminPhone,
                    onValueChange = { adminPhone = it },
                    label = { Text("رقم واتساب المدير الرئيسي", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxPerDay,
                    onValueChange = { maxPerDay = it },
                    label = { Text("الحد الأقصى للموظفين في اليوم الواحد", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("السماح بنصف يوم", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = allowHalf, onCheckedChange = { allowHalf = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إشعارات واتساب تلقائية", fontSize = 13.sp, color = TextPrimary)
                    Switch(checked = waEnabled, onCheckedChange = { waEnabled = it })
                }

                Text("⚡ إعدادات اتصال سوبابيز (Supabase)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryPurpleLight)

                OutlinedTextField(
                    value = supabaseUrl,
                    onValueChange = { supabaseUrl = it },
                    label = { Text("رابط مشروع Supabase URL", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supabaseKey,
                    onValueChange = { supabaseKey = it },
                    label = { Text("مفتاح Supabase Anon Key", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val updated = settings.copy(
                            systemName = systemName,
                            adminPhone = adminPhone,
                            maxPerDay = maxPerDay.toIntOrNull() ?: 1,
                            halfDayMinutes = halfMins.toIntOrNull() ?: 270,
                            allowHalfDay = allowHalf,
                            waEnabled = waEnabled,
                            supabaseUrl = supabaseUrl,
                            supabaseAnonKey = supabaseKey
                        )
                        onUpdateSettings(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ الإعدادات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Danger zone
        Card(
            colors = CardDefaults.cardColors(containerColor = DangerRedContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚠️ منطقة الخطر - تصفير النظام", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                Text("سيتم حذف جميع البيانات والعودة للضبط المصنعي الإبتدائي.", fontSize = 11.sp, color = TextSecondary)
                Button(
                    onClick = onResetSystem,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تصفير النظام كاملاً", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
