package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActiveDialogHandler
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.LeavesTheme
import com.example.ui.viewmodel.DialogState
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LeavesTheme {
                // Force RTL Layout Direction for Arabic UI
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                    val employees by viewModel.employees.collectAsStateWithLifecycle()
                    val users by viewModel.users.collectAsStateWithLifecycle()
                    val leaves by viewModel.leaves.collectAsStateWithLifecycle()
                    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                    val settings by viewModel.settings.collectAsStateWithLifecycle()
                    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()
                    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()

                    val snackbarHostState = remember { SnackbarHostState() }

                    // Handle Alert messages as Snackbars
                    LaunchedEffect(alertMessage) {
                        alertMessage?.let { (msg, _) ->
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearAlert()
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (currentUser != null && currentScreen != "login" && currentScreen != "reset") {
                                AppTopBar(
                                    currentUser = currentUser,
                                    notifications = notifications,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        },
                        bottomBar = {
                            if (currentUser != null && currentScreen in listOf("dash", "entry", "records", "balance")) {
                                val pendingCount = leaves.count { it.status == "pending" }
                                AppBottomNavBar(
                                    selectedScreen = currentScreen,
                                    pendingBadgeCount = if (currentUser?.role == "admin") pendingCount else 0,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (currentUser == null || currentScreen == "login") {
                                LoginScreen(
                                    onLoginSubmit = { email, pass -> viewModel.login(email, pass) },
                                    onResetPasswordClick = { viewModel.navigateTo("reset") }
                                )
                            } else {
                                AnimatedContent(
                                    targetState = currentScreen,
                                    label = "screen_transition"
                                ) { screen ->
                                    when (screen) {
                                        "dash" -> HomeScreen(
                                            currentUser = currentUser,
                                            employees = employees,
                                            leaves = leaves,
                                            onNavigate = { viewModel.navigateTo(it) },
                                            onApproveLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.WhatsAppModal(
                                                        title = "إشعار قبول الإجازة",
                                                        name = leave.empName,
                                                        phone = leave.empPhone,
                                                        message = "✅ تم قبول طلب إجازتك (${leave.days} يوم) من ${leave.from} إلى ${leave.to}. 🎉"
                                                    )
                                                )
                                                viewModel.approveLeave(leave)
                                            },
                                            onRejectLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.RejectModal(
                                                        leave = leave,
                                                        onConfirm = { reason ->
                                                            viewModel.rejectLeave(leave, reason)
                                                        }
                                                    )
                                                )
                                            }
                                        )

                                        "entry" -> LeaveRequestTab(
                                            currentUser = currentUser,
                                            employees = employees,
                                            allowHalfDay = settings.allowHalfDay,
                                            onSubmitRequest = { from, to, half, type, notes ->
                                                viewModel.submitLeaveRequest(from, to, half, type, notes)
                                            }
                                        )

                                        "records" -> MyRecordsTab(
                                            currentUser = currentUser,
                                            leaves = leaves,
                                            onApproveLeave = { viewModel.approveLeave(it) },
                                            onRejectLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.RejectModal(
                                                        leave = leave,
                                                        onConfirm = { reason -> viewModel.rejectLeave(leave, reason) }
                                                    )
                                                )
                                            },
                                            onDeleteLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.ConfirmModal(
                                                        title = "حذف الإجازة",
                                                        message = "هل تريد حذف إجازة ${leave.empName} (${leave.from} - ${leave.to}) نهائياً؟",
                                                        onConfirm = { viewModel.deleteLeave(leave) }
                                                    )
                                                )
                                            }
                                        )

                                        "balance" -> BalancesTab(employees = employees)

                                        "upcoming" -> UpcomingLeavesScreen(
                                            leaves = leaves,
                                            employees = employees,
                                            onBack = { viewModel.navigateTo("dash") }
                                        )

                                        "admin" -> AdminScreen(
                                            currentUser = currentUser,
                                            employees = employees,
                                            users = users,
                                            leaves = leaves,
                                            settings = settings,
                                            onApproveLeave = { viewModel.approveLeave(it) },
                                            onRejectLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.RejectModal(
                                                        leave = leave,
                                                        onConfirm = { reason -> viewModel.rejectLeave(leave, reason) }
                                                    )
                                                )
                                            },
                                            onDeleteLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.ConfirmModal(
                                                        title = "حذف الإجازة",
                                                        message = "هل تريد حذف الإجازة نهائياً؟",
                                                        onConfirm = { viewModel.deleteLeave(leave) }
                                                    )
                                                )
                                            },
                                            onAddEmployee = { name, phone, annual -> viewModel.addEmployee(name, phone, annual) },
                                            onDeleteEmployee = { viewModel.deleteEmployee(it) },
                                            onUpdateContainer = { empId, mins, isAdd -> viewModel.updateContainerBalance(empId, mins, isAdd) },
                                            onUpdateAnnual = { empId, days, isAdd -> viewModel.updateAnnualBalance(empId, days, isAdd) },
                                            onAddUser = { email, phone, pass, staffId, role -> viewModel.addUser(email, phone, pass, staffId, role) },
                                            onDeleteUser = { viewModel.deleteUser(it) },
                                            onUpdatePermissions = { userId, perms -> viewModel.updateUserPermissions(userId, perms) },
                                            onUpdateSettings = { viewModel.updateSettings(it) },
                                            onResetSystem = {
                                                viewModel.showDialog(
                                                    DialogState.ConfirmModal(
                                                        title = "تصفير النظام كاملاً",
                                                        message = "سيتم حذف جميع البيانات والعودة للوضع الإبتدائي. هل أنت متأكد؟",
                                                        onConfirm = { viewModel.resetAllSystemData() }
                                                    )
                                                )
                                            },
                                            onBack = { viewModel.navigateTo("dash") }
                                        )

                                        "profile" -> ProfileScreen(
                                            currentUser = currentUser,
                                            employees = employees,
                                            leaves = leaves,
                                            onUpdateInfo = { name, phone -> viewModel.updateProfile(name, phone) },
                                            onChangePassword = { oldP, newP, confP -> viewModel.changePassword(oldP, newP, confP) },
                                            onBack = { viewModel.navigateTo("dash") }
                                        )

                                        "reset" -> ResetPasswordScreen(
                                            users = users,
                                            onPasswordResetSuccess = { user, newPass ->
                                                viewModel.changePassword("", newPass, newPass)
                                            },
                                            onBack = { viewModel.navigateTo("login") }
                                        )

                                        else -> HomeScreen(
                                            currentUser = currentUser,
                                            employees = employees,
                                            leaves = leaves,
                                            onNavigate = { viewModel.navigateTo(it) },
                                            onApproveLeave = { viewModel.approveLeave(it) },
                                            onRejectLeave = { leave ->
                                                viewModel.showDialog(
                                                    DialogState.RejectModal(
                                                        leave = leave,
                                                        onConfirm = { reason -> viewModel.rejectLeave(leave, reason) }
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Render Dialogs
                            ActiveDialogHandler(
                                dialogState = activeDialog,
                                onDismiss = { viewModel.dismissDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
