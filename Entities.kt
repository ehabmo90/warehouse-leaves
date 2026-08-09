package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val phone: String,
    val annual: Int,
    val customContainer: Int,
    val consumedAnnual: Int,
    val consumedAllowance: Int,
    val consumedContainer: Int,
    val allowanceByMonthJson: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val staffId: Int?,
    val email: String,
    val phone: String,
    val hash: String,
    val salt: String,
    val role: String,
    val name: String,
    val mustChangePass: Boolean,
    val permsJson: String,
    val notifPrefsJson: String
)

@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey val id: Long,
    val empId: Int,
    val empName: String,
    val empPhone: String,
    val fromDate: String,
    val toDate: String,
    val days: Float,
    val halfDay: Boolean,
    val ltype: String,
    val notes: String,
    val status: String,
    val at: String,
    val approvedAt: String?,
    val approvedBy: String?,
    val rejectReason: String?,
    val allowanceMonthKey: String?
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val leaveId: Long?,
    val targetRole: String,
    val read: Boolean,
    val time: String
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val id: Int = 1,
    val systemName: String,
    val adminPhone: String,
    val maxPerDay: Int,
    val allowHalfDay: Boolean,
    val halfDayMinutes: Int,
    val waEnabled: Boolean,
    val showDashOnLogin: Boolean,
    val sessionTimeoutMin: Int,
    val supabaseUrl: String,
    val supabaseAnonKey: String
)

@Entity(tableName = "audit_logs")
data class AuditEntity(
    @PrimaryKey val id: Long,
    val actor: String,
    val action: String,
    val details: String,
    val time: String
)
