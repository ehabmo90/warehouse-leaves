package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.DialogState

@Composable
fun ActiveDialogHandler(
    dialogState: DialogState?,
    onDismiss: () -> Unit
) {
    if (dialogState == null) return
    val context = LocalContext.current

    when (dialogState) {
        is DialogState.ConfirmModal -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceCard1,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dialogState.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dialogState.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(text = dialogState.message, color = TextSecondary, fontSize = 13.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            dialogState.onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text(dialogState.confirmText, color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextMuted)
                    }
                }
            )
        }

        is DialogState.RejectModal -> {
            var reason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceCard1,
                title = {
                    Text(text = "❌ سبب رفض الإجازة", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text(text = "أدخل سبب الرفض لإبلاغ الموظف (${dialogState.leave.empName}):", color = TextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            placeholder = { Text("مثال: الضغط كبيراً في هذا التاريخ...", color = TextMuted) },
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
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalReason = if (reason.isBlank()) "رُفض من قبل المدير" else reason
                            dialogState.onConfirm(finalReason)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("تأكيد الرفض", color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextMuted)
                    }
                }
            )
        }

        is DialogState.WhatsAppModal -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = SurfaceCard1,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💬", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dialogState.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AccentGreenContainer, RoundedCornerShape(10.dp))
                            .border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "الاسم: ${dialogState.name}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "📱 ${dialogState.phone}", color = AccentGreen, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = dialogState.message, color = TextSecondary, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanNum = dialogState.phone.replace("\\D".toRegex(), "")
                            if (cleanNum.length >= 9) {
                                val url = "https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode(dialogState.message)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("إرسال واتساب", color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismiss) {
                        Text("تخطي", color = TextMuted)
                    }
                }
            )
        }
    }
}
