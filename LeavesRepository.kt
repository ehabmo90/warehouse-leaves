package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.models.*
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*

class LeavesRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val employeeDao = db.employeeDao()
    private val userDao = db.userDao()
    private val leaveDao = db.leaveDao()
    private val notificationDao = db.notificationDao()
    private val settingDao = db.settingDao()
    private val auditDao = db.auditDao()

    private val supabaseService = SupabaseService(
        baseUrl = BuildConfig.SUPABASE_URL,
        apiKey = BuildConfig.SUPABASE_ANON_KEY
    )

    // Flow sources
    val employees: Flow<List<Employee>> = employeeDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    val users: Flow<List<User>> = userDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    val leaves: Flow<List<Leave>> = leaveDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    val notifications: Flow<List<NotificationItem>> = notificationDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    val settings: Flow<AppSettings> = settingDao.getSettings().map { entity ->
        entity?.toModel() ?: AppSettings()
    }

    val auditLogs: Flow<List<AuditLog>> = auditDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    // Default staff definitions
    companion object {
        val DEFAULT_STAFF = listOf(
            Employee(1, "أحمد غانم", "201112340090"),
            Employee(2, "محمد صابر", "201110122753"),
            Employee(3, "سعد إبراهيم", "201144452245"),
            Employee(4, "أحمد كرم", "201142097956"),
            Employee(5, "أحمد عيد", "201011538864"),
            Employee(6, "محمد عواد", "201127330012"),
            Employee(7, "إسلام هارون", "201142725264"),
            Employee(8, "محمد عصام", "201100969643"),
            Employee(9, "أحمد شعبان", "201016291887")
        )

        val EGYPTIAN_HOLIDAYS = setOf(
            "2025-01-07", "2025-04-25", "2025-04-28", "2025-05-01",
            "2025-06-05", "2025-06-06", "2025-06-30", "2025-07-23",
            "2025-10-06", "2025-11-12",
            "2026-01-07", "2026-04-17", "2026-04-20", "2026-05-01",
            "2026-06-05", "2026-06-06", "2026-06-30", "2026-07-23",
            "2026-10-06"
        )

        fun genSalt(): String {
            val random = SecureRandom()
            val bytes = ByteArray(16)
            random.nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun hashPass(pass: String, salt: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val input = "$salt:$pass:v5".toByteArray()
            val digest = md.digest(input)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val currentSettings = settingDao.getSettings().firstOrNull()
        if (currentSettings == null) {
            val initialSettings = SettingEntity(
                id = 1,
                systemName = "نظام إجازات المخازن",
                adminPhone = "201021501914",
                maxPerDay = 1,
                allowHalfDay = true,
                halfDayMinutes = 270,
                waEnabled = true,
                showDashOnLogin = true,
                sessionTimeoutMin = 60,
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
            )
            settingDao.insert(initialSettings)
        } else {
            var updatedUrl = currentSettings.supabaseUrl
            var updatedKey = currentSettings.supabaseAnonKey
            var changed = false
            if (updatedUrl.isBlank() || updatedUrl.contains("your-project-url")) {
                updatedUrl = BuildConfig.SUPABASE_URL
                changed = true
            }
            if (updatedKey.isBlank() || updatedKey.contains("your-anon-public-key")) {
                updatedKey = BuildConfig.SUPABASE_ANON_KEY
                changed = true
            }
            if (changed) {
                val newSettings = currentSettings.copy(supabaseUrl = updatedUrl, supabaseAnonKey = updatedKey)
                settingDao.insert(newSettings)
            }
            supabaseService.updateConfig(updatedUrl, updatedKey)
        }

        val existingEmps = employeeDao.getAll().firstOrNull() ?: emptyList()
        if (existingEmps.isEmpty()) {
            val empEntities = DEFAULT_STAFF.map { it.toEntity() }
            employeeDao.insertAll(empEntities)
        }

        val existingUsers = userDao.getAll().firstOrNull() ?: emptyList()
        if (existingUsers.isEmpty()) {
            val defaultUsersRaw = listOf(
                User(0, null, "ahmed.ghanem@wh.com", "201021501914", "", "", "admin", "مدير النظام", true, PermissionDefs.defaultAdmin),
                User(1, 1, "ehabmostafa907@gmail.com", "201147669613", "", "", "user", "أحمد غانم", true, PermissionDefs.defaultUser),
                User(2, 2, "mohamed.saber@wh.com", "201110122753", "", "", "user", "محمد صابر", true, PermissionDefs.defaultUser),
                User(3, 3, "saad.ibrahim@wh.com", "201144452245", "", "", "user", "سعد إبراهيم", true, PermissionDefs.defaultUser),
                User(4, 4, "ahmed.karam@wh.com", "201142097956", "", "", "user", "أحمد كرم", true, PermissionDefs.defaultUser),
                User(5, 5, "ahmed.eid@wh.com", "201011538864", "", "", "user", "أحمد عيد", true, PermissionDefs.defaultUser),
                User(6, 6, "mohamed.awad@wh.com", "201127330012", "", "", "user", "محمد عواد", true, PermissionDefs.defaultUser),
                User(7, 7, "islam.haroon@wh.com", "201142725264", "", "", "user", "إسلام هارون", true, PermissionDefs.defaultUser),
                User(8, 8, "mohamed.essam@wh.com", "201100969643", "", "", "user", "محمد عصام", true, PermissionDefs.defaultUser),
                User(9, 9, "ahmed.shaaban@wh.com", "201016291887", "", "", "user", "أحمد شعبان", true, PermissionDefs.defaultUser)
            )

            val initialUserEntities = defaultUsersRaw.map { user ->
                val salt = genSalt()
                val initPass = when(user.id.toInt()) {
                    0, 1 -> "Ghanem@Init"
                    2 -> "Saber@Init"
                    3 -> "Saad@Init"
                    4 -> "Karam@Init"
                    5 -> "Eid@Init"
                    6 -> "Awad@Init"
                    7 -> "Haroon@Init"
                    8 -> "Essam@Init"
                    else -> "Shaaban@Init"
                }
                val hash = hashPass(initPass, salt)
                user.copy(salt = salt, hash = hash).toEntity()
            }
            userDao.insertAll(initialUserEntities)
        }
    }

    // Calculations
    fun calculateWorkDays(fromStr: String, toStr: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return try {
            val fromDate = sdf.parse(fromStr) ?: return 0
            val toDate = sdf.parse(toStr) ?: return 0
            if (fromDate.after(toDate)) return 0

            val cal = Calendar.getInstance()
            cal.time = fromDate
            var count = 0

            while (!cal.time.after(toDate)) {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val currentStr = sdf.format(cal.time)

                // Friday is Calendar.FRIDAY (6)
                if (dayOfWeek != Calendar.FRIDAY && !EGYPTIAN_HOLIDAYS.contains(currentStr)) {
                    count++
                }
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            count
        } catch (e: Exception) {
            0
        }
    }

    fun calculateLeaveDays(fromStr: String, toStr: String, isHalfDay: Boolean, halfDayMinutes: Int): Float {
        val fullDays = calculateWorkDays(fromStr, toStr)
        if (fullDays <= 0) return 0f
        if (!isHalfDay) return fullDays.toFloat()

        val fraction = halfDayMinutes / 540f
        return Math.max(0f, fullDays - (1f - fraction))
    }

    // Business Logic Actions
    suspend fun insertLeave(leave: Leave) = withContext(Dispatchers.IO) {
        leaveDao.insert(leave.toEntity())
        addAuditLog(leave.empName, "request_leave", "طلب إجازة جديد (${leave.days} يوم - ${leave.from} إلى ${leave.to})")
        syncToSupabase("leaves", leave.toEntity().toJsonObject())
    }

    suspend fun updateLeaveStatus(
        leave: Leave,
        newStatus: String,
        approvedBy: String,
        rejectReason: String? = null
    ) = withContext(Dispatchers.IO) {
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        val updatedLeave = leave.copy(
            status = newStatus,
            approvedAt = nowIso,
            approvedBy = approvedBy,
            rejectReason = rejectReason
        )

        // Deduct/Restore balances if status is approved/rejected
        if (newStatus == "approved") {
            val emps = employeeDao.getAll().firstOrNull() ?: emptyList()
            val empEntity = emps.find { it.id == leave.empId }
            if (empEntity != null) {
                val emp = empEntity.toModel()
                val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                var newConsumedAnnual = emp.consumedAnnual
                var newConsumedAllowance = emp.consumedAllowance
                var newConsumedContainer = emp.consumedContainer
                val updatedAllowanceByMonth = emp.allowanceByMonth.toMutableMap()

                when (leave.ltype) {
                    "annual" -> newConsumedAnnual += leave.days.toInt()
                    "allowance" -> {
                        val currentMonthUsage = updatedAllowanceByMonth[monthKey] ?: 0
                        updatedAllowanceByMonth[monthKey] = currentMonthUsage + leave.days.toInt()
                        newConsumedAllowance += leave.days.toInt()
                    }
                    "container" -> {
                        val minsUsed = Math.round(leave.days * 540)
                        newConsumedContainer += minsUsed
                    }
                }

                val updatedEmp = emp.copy(
                    consumedAnnual = newConsumedAnnual,
                    consumedAllowance = newConsumedAllowance,
                    consumedContainer = newConsumedContainer,
                    allowanceByMonth = updatedAllowanceByMonth
                )
                employeeDao.insert(updatedEmp.toEntity())
                syncToSupabase("employees", updatedEmp.toEntity().toJsonObject())
            }
        }

        leaveDao.insert(updatedLeave.toEntity())
        addAuditLog(approvedBy, if (newStatus == "approved") "approve_leave" else "reject_leave", "${if (newStatus == "approved") "قبول" else "رفض"} إجازة ${leave.empName}")
        syncToSupabase("leaves", updatedLeave.toEntity().toJsonObject())
    }

    suspend fun deleteLeave(leave: Leave, actor: String) = withContext(Dispatchers.IO) {
        // Restore balance if was approved
        if (leave.status == "approved") {
            val emps = employeeDao.getAll().firstOrNull() ?: emptyList()
            val empEntity = emps.find { it.id == leave.empId }
            if (empEntity != null) {
                val emp = empEntity.toModel()
                var newConsumedAnnual = Math.max(0, emp.consumedAnnual - leave.days.toInt())
                var newConsumedAllowance = Math.max(0, emp.consumedAllowance - leave.days.toInt())
                var newConsumedContainer = Math.max(0, emp.consumedContainer - Math.round(leave.days * 540))

                val updatedEmp = emp.copy(
                    consumedAnnual = newConsumedAnnual,
                    consumedAllowance = newConsumedAllowance,
                    consumedContainer = newConsumedContainer
                )
                employeeDao.insert(updatedEmp.toEntity())
            }
        }
        leaveDao.deleteById(leave.id)
        addAuditLog(actor, "delete_leave", "حذف إجازة ${leave.empName}")
    }

    suspend fun saveEmployee(employee: Employee, actor: String) = withContext(Dispatchers.IO) {
        employeeDao.insert(employee.toEntity())
        addAuditLog(actor, "save_employee", "حفظ بيانات الموظف ${employee.name}")
        syncToSupabase("employees", employee.toEntity().toJsonObject())
    }

    suspend fun deleteEmployee(empId: Int, actor: String) = withContext(Dispatchers.IO) {
        employeeDao.deleteById(empId)
        addAuditLog(actor, "delete_employee", "حذف الموظف رقم $empId")
    }

    suspend fun saveUser(user: User, actor: String) = withContext(Dispatchers.IO) {
        userDao.insert(user.toEntity())
        addAuditLog(actor, "save_user", "حفظ حساب المستخدم ${user.email}")
        syncToSupabase("users", user.toEntity().toJsonObject())
    }

    suspend fun deleteUser(userId: Long, actor: String) = withContext(Dispatchers.IO) {
        userDao.deleteById(userId)
        addAuditLog(actor, "delete_user", "حذف المستخدم رقم $userId")
    }

    suspend fun saveSettings(settings: AppSettings, actor: String) = withContext(Dispatchers.IO) {
        settingDao.insert(settings.toEntity())
        supabaseService.updateConfig(settings.supabaseUrl, settings.supabaseAnonKey)
        addAuditLog(actor, "save_settings", "تحديث إعدادات النظام")
        syncToSupabase("settings", settings.toEntity().toJsonObject())
    }

    suspend fun addNotification(notification: NotificationItem) = withContext(Dispatchers.IO) {
        notificationDao.insert(notification.toEntity())
    }

    suspend fun addAuditLog(actor: String, action: String, details: String) = withContext(Dispatchers.IO) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val audit = AuditLog(
            id = System.currentTimeMillis() + (0..999).random(),
            actor = actor,
            action = action,
            details = details,
            time = nowStr
        )
        auditDao.insert(audit.toEntity())
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        employeeDao.deleteAll()
        userDao.deleteAll()
        leaveDao.deleteAll()
        notificationDao.deleteAll()
        auditDao.deleteAll()
        initializeIfNeeded()
    }

    private suspend fun syncToSupabase(tableName: String, jsonObj: JSONObject) {
        try {
            val jsonArray = JSONArray().apply { put(jsonObj) }
            supabaseService.upsertTable(tableName, jsonArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Converters Extension Functions
private fun EmployeeEntity.toModel(): Employee {
    val allowanceMap = mutableMapOf<String, Int>()
    try {
        val json = JSONObject(allowanceByMonthJson)
        json.keys().forEach { key -> allowanceMap[key] = json.getInt(key) }
    } catch (_: Exception) {}

    return Employee(
        id = id,
        name = name,
        phone = phone,
        annual = annual,
        customContainer = customContainer,
        consumedAnnual = consumedAnnual,
        consumedAllowance = consumedAllowance,
        consumedContainer = consumedContainer,
        allowanceByMonth = allowanceMap
    )
}

private fun Employee.toEntity(): EmployeeEntity {
    val jsonMap = JSONObject(allowanceByMonth as Map<*, *>).toString()
    return EmployeeEntity(
        id = id,
        name = name,
        phone = phone,
        annual = annual,
        customContainer = customContainer,
        consumedAnnual = consumedAnnual,
        consumedAllowance = consumedAllowance,
        consumedContainer = consumedContainer,
        allowanceByMonthJson = jsonMap
    )
}

private fun EmployeeEntity.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("phone", phone)
        put("annual", annual)
        put("custom_container", customContainer)
        put("consumed_annual", consumedAnnual)
        put("consumed_allowance", consumedAllowance)
        put("consumed_container", consumedContainer)
        put("allowance_by_month", JSONObject(allowanceByMonthJson))
    }
}

private fun UserEntity.toModel(): User {
    val permsMap = mutableMapOf<String, Int>()
    try {
        val json = JSONObject(permsJson)
        json.keys().forEach { key -> permsMap[key] = json.getInt(key) }
    } catch (_: Exception) {}

    val prefsMap = mutableMapOf<String, Boolean>()
    try {
        val json = JSONObject(notifPrefsJson)
        json.keys().forEach { key -> prefsMap[key] = json.getBoolean(key) }
    } catch (_: Exception) {}

    return User(
        id = id,
        staffId = staffId,
        email = email,
        phone = phone,
        hash = hash,
        salt = salt,
        role = role,
        name = name,
        mustChangePass = mustChangePass,
        perms = if (permsMap.isNotEmpty()) permsMap else if (role == "admin") PermissionDefs.defaultAdmin else PermissionDefs.defaultUser,
        notifPrefs = if (prefsMap.isNotEmpty()) prefsMap else mapOf("wa_on_approve" to true, "wa_on_reject" to true, "show_balance_on_dash" to true, "dark_mode" to false)
    )
}

private fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        staffId = staffId,
        email = email,
        phone = phone,
        hash = hash,
        salt = salt,
        role = role,
        name = name,
        mustChangePass = mustChangePass,
        permsJson = JSONObject(perms as Map<*, *>).toString(),
        notifPrefsJson = JSONObject(notifPrefs as Map<*, *>).toString()
    )
}

private fun UserEntity.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("staff_id", staffId ?: JSONObject.NULL)
        put("email", email)
        put("phone", phone)
        put("hash", hash)
        put("salt", salt)
        put("role", role)
        put("name", name)
        put("must_change_pass", mustChangePass)
        put("perms", JSONObject(permsJson))
        put("notif_prefs", JSONObject(notifPrefsJson))
    }
}

private fun LeaveEntity.toModel(): Leave {
    return Leave(
        id = id,
        empId = empId,
        empName = empName,
        empPhone = empPhone,
        from = fromDate,
        to = toDate,
        days = days,
        halfDay = halfDay,
        ltype = ltype,
        notes = notes,
        status = status,
        at = at,
        approvedAt = approvedAt,
        approvedBy = approvedBy,
        rejectReason = rejectReason,
        allowanceMonthKey = allowanceMonthKey
    )
}

private fun Leave.toEntity(): LeaveEntity {
    return LeaveEntity(
        id = id,
        empId = empId,
        empName = empName,
        empPhone = empPhone,
        fromDate = from,
        toDate = to,
        days = days,
        halfDay = halfDay,
        ltype = ltype,
        notes = notes,
        status = status,
        at = at,
        approvedAt = approvedAt,
        approvedBy = approvedBy,
        rejectReason = rejectReason,
        allowanceMonthKey = allowanceMonthKey
    )
}

private fun LeaveEntity.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("emp_id", empId)
        put("emp_name", empName)
        put("emp_phone", empPhone)
        put("from_date", fromDate)
        put("to_date", toDate)
        put("days", days)
        put("half_day", halfDay)
        put("ltype", ltype)
        put("notes", notes)
        put("status", status)
        put("at", at)
        put("approved_at", approvedAt ?: JSONObject.NULL)
        put("approved_by", approvedBy ?: JSONObject.NULL)
        put("reject_reason", rejectReason ?: JSONObject.NULL)
        put("allowance_month_key", allowanceMonthKey ?: JSONObject.NULL)
    }
}

private fun NotificationEntity.toModel(): NotificationItem {
    return NotificationItem(
        id = id,
        text = text,
        leaveId = leaveId,
        targetRole = targetRole,
        read = read,
        time = time
    )
}

private fun NotificationItem.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        text = text,
        leaveId = leaveId,
        targetRole = targetRole,
        read = read,
        time = time
    )
}

private fun SettingEntity.toModel(): AppSettings {
    return AppSettings(
        id = id,
        systemName = systemName,
        adminPhone = adminPhone,
        maxPerDay = maxPerDay,
        allowHalfDay = allowHalfDay,
        halfDayMinutes = halfDayMinutes,
        waEnabled = waEnabled,
        showDashOnLogin = showDashOnLogin,
        sessionTimeoutMin = sessionTimeoutMin,
        supabaseUrl = supabaseUrl,
        supabaseAnonKey = supabaseAnonKey
    )
}

private fun AppSettings.toEntity(): SettingEntity {
    return SettingEntity(
        id = id,
        systemName = systemName,
        adminPhone = adminPhone,
        maxPerDay = maxPerDay,
        allowHalfDay = allowHalfDay,
        halfDayMinutes = halfDayMinutes,
        waEnabled = waEnabled,
        showDashOnLogin = showDashOnLogin,
        sessionTimeoutMin = sessionTimeoutMin,
        supabaseUrl = supabaseUrl,
        supabaseAnonKey = supabaseAnonKey
    )
}

private fun SettingEntity.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("system_name", systemName)
        put("admin_phone", adminPhone)
        put("max_per_day", maxPerDay)
        put("allow_half_day", allowHalfDay)
        put("half_day_minutes", halfDayMinutes)
        put("wa_enabled", waEnabled)
        put("show_dash_on_login", showDashOnLogin)
        put("session_timeout_min", sessionTimeoutMin)
        put("supabase_url", supabaseUrl)
        put("supabase_anon_key", supabaseAnonKey)
    }
}

private fun AuditEntity.toModel(): AuditLog {
    return AuditLog(
        id = id,
        actor = actor,
        action = action,
        details = details,
        time = time
    )
}

private fun AuditLog.toEntity(): AuditEntity {
    return AuditEntity(
        id = id,
        actor = actor,
        action = action,
        details = details,
        time = time
    )
}
