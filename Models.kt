package com.example.data.models

import androidx.annotation.Keep

data class Employee(
    val id: Int,
    val name: String,
    val phone: String = "",
    val annual: Int = 21,
    val customContainer: Int = 0, // in minutes
    val consumedAnnual: Int = 0,
    val consumedAllowance: Int = 0,
    val consumedContainer: Int = 0, // in minutes
    val allowanceByMonth: Map<String, Int> = emptyMap() // e.g. "2026-08" -> days
)

data class User(
    val id: Long,
    val staffId: Int? = null,
    val email: String,
    val phone: String = "",
    val hash: String = "",
    val salt: String = "",
    val role: String = "user", // "admin" or "user"
    val name: String = "",
    val mustChangePass: Boolean = false,
    val perms: Map<String, Int> = emptyMap(), // 0: None, 1: Read, 2: Write
    val notifPrefs: Map<String, Boolean> = mapOf(
        "wa_on_approve" to true,
        "wa_on_reject" to true,
        "show_balance_on_dash" to true,
        "dark_mode" to false
    )
)

data class Leave(
    val id: Long,
    val empId: Int,
    val empName: String,
    val empPhone: String = "",
    val from: String, // YYYY-MM-DD
    val to: String,   // YYYY-MM-DD
    val days: Float,
    val halfDay: Boolean = false,
    val ltype: String = "annual", // "annual", "allowance", "container"
    val notes: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val at: String = "", // ISO timestamp
    val approvedAt: String? = null,
    val approvedBy: String? = null,
    val rejectReason: String? = null,
    val allowanceMonthKey: String? = null
)

data class NotificationItem(
    val id: Long,
    val text: String,
    val leaveId: Long? = null,
    val targetRole: String = "admin", // "admin", "user", "all"
    val read: Boolean = false,
    val time: String = ""
)

data class AppSettings(
    val id: Int = 1,
    val systemName: String = "نظام إجازات المخازن",
    val adminPhone: String = "201021501914",
    val maxPerDay: Int = 1,
    val allowHalfDay: Boolean = true,
    val halfDayMinutes: Int = 270,
    val waEnabled: Boolean = true,
    val showDashOnLogin: Boolean = true,
    val sessionTimeoutMin: Int = 60,
    val supabaseUrl: String = "https://your-project-url.supabase.co",
    val supabaseAnonKey: String = "your-anon-public-key"
)

data class AuditLog(
    val id: Long,
    val actor: String,
    val action: String,
    val details: String = "",
    val time: String = ""
)

object PermissionDefs {
    data class PermDef(val key: String, val label: String, val desc: String)

    val list = listOf(
        PermDef("view_dashboard", "عرض الداشبورد", "رؤية الإحصائيات والتقويم العام"),
        PermDef("request_leave", "تقديم طلب إجازة", "إرسال طلب إجازة جديد"),
        PermDef("view_own_leaves", "سجل إجازاتي", "رؤية طلباته وسجلاته الشخصية"),
        PermDef("view_all_leaves", "عرض كل السجلات", "رؤية إجازات جميع الموظفين"),
        PermDef("approve_leaves", "الموافقة / الرفض", "قبول أو رفض طلبات الإجازة"),
        PermDef("view_balances", "عرض الأرصدة", "رؤية أرصدة الموظفين"),
        PermDef("edit_balances", "تعديل الأرصدة", "منح وعاء أو تعديل رصيد سنوي"),
        PermDef("manage_users", "إدارة المستخدمين", "إضافة / حذف حسابات"),
        PermDef("manage_perms", "إدارة الصلاحيات", "تعديل صلاحيات الآخرين"),
        PermDef("export_reports", "تصدير التقارير", "تحميل Excel / PDF"),
        PermDef("view_settings", "عرض الإعدادات", "رؤية إعدادات النظام"),
        PermDef("edit_settings", "تعديل الإعدادات", "تغيير إعدادات النظام")
    )

    val defaultAdmin = mapOf(
        "view_dashboard" to 2, "request_leave" to 0, "view_own_leaves" to 2,
        "view_all_leaves" to 2, "approve_leaves" to 2, "view_balances" to 2,
        "edit_balances" to 2, "manage_users" to 2, "manage_perms" to 2,
        "export_reports" to 2, "view_settings" to 2, "edit_settings" to 2
    )

    val defaultUser = mapOf(
        "view_dashboard" to 2, "request_leave" to 2, "view_own_leaves" to 2,
        "view_all_leaves" to 0, "approve_leaves" to 0, "view_balances" to 1,
        "edit_balances" to 0, "manage_users" to 0, "manage_perms" to 0,
        "export_reports" to 0, "view_settings" to 0, "edit_settings" to 0
    )
}
