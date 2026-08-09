package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.LeavesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = LeavesRepository(application.applicationContext)

    // Data streams
    val employees: StateFlow<List<Employee>> = repository.employees.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val users: StateFlow<List<User>> = repository.users.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val leaves: StateFlow<List<Leave>> = repository.leaves.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()
    )

    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current User Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation Screen ("dash", "entry", "records", "balance", "upcoming", "admin", "profile", "reset")
    private val _currentScreen = MutableStateFlow("dash")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Alert Messages
    private val _alertMessage = MutableStateFlow<Pair<String, Boolean>?>(null) // Message, isError
    val alertMessage: StateFlow<Pair<String, Boolean>?> = _alertMessage.asStateFlow()

    // Dialog States
    private val _activeDialog = MutableStateFlow<DialogState?>(null)
    val activeDialog: StateFlow<DialogState?> = _activeDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
            // Auto login default admin if no session
            users.collect { userList ->
                if (_currentUser.value == null && userList.isNotEmpty()) {
                    val admin = userList.find { it.role == "admin" } ?: userList.first()
                    _currentUser.value = admin
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun showAlert(msg: String, isError: Boolean = false) {
        _alertMessage.value = Pair(msg, isError)
    }

    fun clearAlert() {
        _alertMessage.value = null
    }

    fun showDialog(state: DialogState) {
        _activeDialog.value = state
    }

    fun dismissDialog() {
        _activeDialog.value = null
    }

    fun login(email: String, pass: String): Boolean {
        val userList = users.value
        val user = userList.find { it.email.equals(email.trim(), ignoreCase = true) }
        if (user == null) {
            showAlert("بيانات الدخول غير صحيحة", isError = true)
            return false
        }

        val computedHash = LeavesRepository.hashPass(pass, user.salt)
        if (computedHash != user.hash) {
            showAlert("كلمة المرور غير صحيحة", isError = true)
            return false
        }

        _currentUser.value = user
        val startScreen = if (settings.value.showDashOnLogin) "dash" else "records"
        _currentScreen.value = startScreen
        showAlert("مرحباً بك ${user.name}", isError = false)
        return true
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = "login"
    }

    // Leave Submission
    fun submitLeaveRequest(
        fromDate: String,
        toDate: String,
        isHalfDay: Boolean,
        leaveType: String,
        notes: String,
        targetStaffId: Int? = null,
        autoApprove: Boolean = false
    ) {
        val user = _currentUser.value ?: run {
            showAlert("الرجاء تسجيل الدخول أولاً", isError = true)
            return
        }

        val staffId = targetStaffId ?: user.staffId ?: run {
            showAlert("يرجى اختيار الموظف أولاً", isError = true)
            return
        }

        val emp = employees.value.find { it.id == staffId } ?: run {
            showAlert("الموظف المحدد غير موجود", isError = true)
            return
        }

        if (fromDate.isBlank() || toDate.isBlank()) {
            showAlert("اختر تاريخ البداية والنهاية", isError = true)
            return
        }

        val rawWorkDays = repository.calculateWorkDays(fromDate, toDate)
        if (rawWorkDays <= 0) {
            showAlert("التواريخ المحددة تقع في أيام عطلة رسمية", isError = true)
            return
        }

        val halfMins = settings.value.halfDayMinutes
        val daysCount = repository.calculateLeaveDays(fromDate, toDate, isHalfDay, halfMins)

        // Self/Employee conflict check
        val currentLeaves = leaves.value
        val selfConflict = currentLeaves.find { l ->
            l.empId == staffId && l.status != "rejected" && !(toDate < l.from || fromDate > l.to)
        }
        if (selfConflict != null) {
            showAlert("للموظف إجازة بالفعل في نفس الفترة (${selfConflict.from} – ${selfConflict.to})", isError = true)
            return
        }

        // Max per day conflict check
        val maxPerDay = settings.value.maxPerDay
        var conflictDay: String? = null
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val start = sdf.parse(fromDate)!!
            val end = sdf.parse(toDate)!!
            val cal = Calendar.getInstance()
            cal.time = start
            while (!cal.time.after(end)) {
                val dayStr = sdf.format(cal.time)
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY && !LeavesRepository.EGYPTIAN_HOLIDAYS.contains(dayStr)) {
                    val count = currentLeaves.count { l ->
                        l.status != "rejected" && l.empId != staffId && dayStr >= l.from && dayStr <= l.to
                    }
                    if (count >= maxPerDay) {
                        conflictDay = dayStr
                        break
                    }
                }
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        } catch (_: Exception) {}

        if (conflictDay != null) {
            showAlert("اليوم $conflictDay محجوز - تم بلوغ الحد الأقصى ($maxPerDay موظف)", isError = true)
            return
        }

        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
        val isAdminAction = user.role == "admin" || (targetStaffId != null && targetStaffId != user.staffId)
        val initialStatus = if (isAdminAction && autoApprove) "approved" else "pending"

        val noteText = if (isAdminAction && targetStaffId != user.staffId) {
            if (notes.isBlank()) "طلب بواسطة الإدارة: ${user.name}" else "$notes (تم الطلب بواسطة الإدارة: ${user.name})"
        } else notes

        val newLeave = Leave(
            id = System.currentTimeMillis() + (0..999).random(),
            empId = staffId,
            empName = emp.name,
            empPhone = emp.phone,
            from = fromDate,
            to = toDate,
            days = daysCount,
            halfDay = isHalfDay,
            ltype = leaveType,
            notes = noteText,
            status = initialStatus,
            at = nowIso,
            approvedAt = if (initialStatus == "approved") nowIso else null,
            approvedBy = if (initialStatus == "approved") user.name else null
        )

        viewModelScope.launch {
            repository.insertLeave(newLeave)
            if (initialStatus == "approved") {
                repository.updateLeaveStatus(newLeave, "approved", user.name)
                showAlert("تم تسجيل وإعتماد إجازة ${emp.name} بنجاح", isError = false)
            } else {
                repository.addNotification(
                    NotificationItem(
                        id = System.currentTimeMillis(),
                        text = "طلب إجازة جديد لـ ${emp.name} ($daysCount يوم)",
                        leaveId = newLeave.id,
                        targetRole = "admin",
                        time = SimpleDateFormat("d MMM, HH:mm", Locale("ar")).format(Date())
                    )
                )
                showAlert("تم إرسال الطلب لـ ${emp.name} بنجاح بانتظار الاعتماد", isError = false)
            }
        }
    }

    fun approveLeave(leave: Leave) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateLeaveStatus(leave, "approved", user.name)
            repository.addNotification(
                NotificationItem(
                    id = System.currentTimeMillis(),
                    text = "تمت الموافقة على إجازتك (${leave.days} يوم)",
                    leaveId = leave.id,
                    targetRole = "user",
                    time = SimpleDateFormat("d MMM, HH:mm", Locale("ar")).format(Date())
                )
            )
            showAlert("تم قبول إجازة ${leave.empName}", isError = false)
        }
    }

    fun rejectLeave(leave: Leave, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateLeaveStatus(leave, "rejected", user.name, reason)
            repository.addNotification(
                NotificationItem(
                    id = System.currentTimeMillis(),
                    text = "تم رفض إجازتك - السبب: $reason",
                    leaveId = leave.id,
                    targetRole = "user",
                    time = SimpleDateFormat("d MMM, HH:mm", Locale("ar")).format(Date())
                )
            )
            showAlert("تم رفض إجازة ${leave.empName}", isError = false)
        }
    }

    fun deleteLeave(leave: Leave) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteLeave(leave, user.name)
            showAlert("تم حذف الإجازة", isError = false)
        }
    }

    // Admin Actions
    fun addEmployee(name: String, phone: String, annual: Int) {
        val user = _currentUser.value ?: return
        if (name.isBlank()) {
            showAlert("أدخل اسم الموظف", isError = true)
            return
        }
        val newId = (employees.value.maxOfOrNull { it.id } ?: 0) + 1
        val emp = Employee(id = newId, name = name, phone = phone, annual = annual)
        viewModelScope.launch {
            repository.saveEmployee(emp, user.name)
            showAlert("تم إضافة الموظف $name بنجاح", isError = false)
        }
    }

    fun updateEmployee(emp: Employee) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.saveEmployee(emp, user.name)
            showAlert("تم تحديث بيانات ${emp.name}", isError = false)
        }
    }

    fun deleteEmployee(empId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteEmployee(empId, user.name)
            showAlert("تم حذف الموظف", isError = false)
        }
    }

    fun updateContainerBalance(empId: Int, mins: Int, isAdd: Boolean) {
        val user = _currentUser.value ?: return
        val emp = employees.value.find { it.id == empId } ?: return
        val newMins = if (isAdd) emp.customContainer + mins else mins
        val updated = emp.copy(customContainer = Math.max(0, newMins))
        viewModelScope.launch {
            repository.saveEmployee(updated, user.name)
            showAlert("تم تحديث وعاء ${emp.name}", isError = false)
        }
    }

    fun updateAnnualBalance(empId: Int, annualDays: Int, isAdd: Boolean) {
        val user = _currentUser.value ?: return
        val emp = employees.value.find { it.id == empId } ?: return
        val newAnnual = if (isAdd) emp.annual + annualDays else annualDays
        val updated = emp.copy(annual = Math.max(0, newAnnual))
        viewModelScope.launch {
            repository.saveEmployee(updated, user.name)
            showAlert("تم تحديث الرصيد السنوي لـ ${emp.name}", isError = false)
        }
    }

    fun addUser(email: String, phone: String, pass: String, staffId: Int?, role: String) {
        val admin = _currentUser.value ?: return
        if (email.isBlank() || pass.length < 6) {
            showAlert("البريد مطلوب وكلمة المرور لا تقل عن 6 أحرف", isError = true)
            return
        }

        val salt = LeavesRepository.genSalt()
        val hash = LeavesRepository.hashPass(pass, salt)
        val staffName = employees.value.find { it.id == staffId }?.name ?: email.substringBefore("@")
        val newUser = User(
            id = System.currentTimeMillis() + (0..999).random(),
            staffId = staffId,
            email = email.trim(),
            phone = phone.trim(),
            hash = hash,
            salt = salt,
            role = role,
            name = staffName,
            mustChangePass = false,
            perms = if (role == "admin") PermissionDefs.defaultAdmin else PermissionDefs.defaultUser
        )

        viewModelScope.launch {
            repository.saveUser(newUser, admin.name)
            showAlert("تم إضافة المستخدم بنجاح", isError = false)
        }
    }

    fun updateUserPermissions(userId: Long, newPerms: Map<String, Int>) {
        val admin = _currentUser.value ?: return
        val targetUser = users.value.find { it.id == userId } ?: return
        val updated = targetUser.copy(perms = newPerms)
        viewModelScope.launch {
            repository.saveUser(updated, admin.name)
            showAlert("تم تحديث الصلاحيات", isError = false)
        }
    }

    fun deleteUser(userId: Long) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteUser(userId, admin.name)
            showAlert("تم حذف المستخدم", isError = false)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            repository.saveSettings(newSettings, admin.name)
            showAlert("تم حفظ الإعدادات", isError = false)
        }
    }

    fun updateProfile(name: String, phone: String) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(name = name, phone = phone)
        _currentUser.value = updatedUser
        viewModelScope.launch {
            repository.saveUser(updatedUser, name)
            if (user.staffId != null) {
                val emp = employees.value.find { it.id == user.staffId }
                if (emp != null) {
                    repository.saveEmployee(emp.copy(name = name, phone = phone), name)
                }
            }
            showAlert("تم حفظ التغييرات الشخصية", isError = false)
        }
    }

    fun changePassword(oldPass: String, newPass: String, confPass: String): Boolean {
        val user = _currentUser.value ?: return false
        if (oldPass.isBlank() || newPass.isBlank() || confPass.isBlank()) {
            showAlert("أدخل كلمات المرور الثلاثة", isError = true)
            return false
        }

        val oldHash = LeavesRepository.hashPass(oldPass, user.salt)
        if (oldHash != user.hash) {
            showAlert("كلمة المرور الحالية غير صحيحة", isError = true)
            return false
        }

        if (newPass != confPass) {
            showAlert("كلمتا المرور غير متطابقتين", isError = true)
            return false
        }

        if (newPass.length < 6) {
            showAlert("كلمة المرور الجديدة لا تقل عن 6 أحرف", isError = true)
            return false
        }

        val newSalt = LeavesRepository.genSalt()
        val newHash = LeavesRepository.hashPass(newPass, newSalt)
        val updatedUser = user.copy(salt = newSalt, hash = newHash, mustChangePass = false)
        _currentUser.value = updatedUser

        viewModelScope.launch {
            repository.saveUser(updatedUser, user.name)
            showAlert("تم تحديث كلمة المرور بنجاح", isError = false)
        }
        return true
    }

    fun resetAllSystemData() {
        viewModelScope.launch {
            repository.resetAllData()
            showAlert("تم إعادة تهيئة النظام كاملاً", isError = false)
            logout()
        }
    }
}

sealed class DialogState {
    data class ConfirmModal(
        val title: String,
        val message: String,
        val icon: String = "⚠️",
        val confirmText: String = "تأكيد",
        val onConfirm: () -> Unit
    ) : DialogState()

    data class RejectModal(
        val leave: Leave,
        val onConfirm: (String) -> Unit
    ) : DialogState()

    data class WhatsAppModal(
        val title: String,
        val name: String,
        val phone: String,
        val message: String
    ) : DialogState()
}
