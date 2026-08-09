package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.User
import com.example.data.repository.LeavesRepository
import com.example.ui.theme.*

@Composable
fun ResetPasswordScreen(
    users: List<User>,
    onPasswordResetSuccess: (User, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) } // 1: Email, 2: OTP, 3: New Pass, 4: Success
    var emailInput by remember { mutableStateOf("") }
    var targetUser by remember { mutableStateOf<User?>(null) }

    var otpInput by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }

    var newPass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(SurfaceCard2, DarkBg),
                    radius = 1200f
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard1.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .border(1.dp, BorderMedium, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(colors = listOf(DangerRed, PrimaryPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("استعادة كلمة المرور", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Text(
                    "التحقق يتم عبر كود SMS يُرسل للواتساب المسجَّل",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Display
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DangerRedContainer)
                            .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(text = errorMessage!!, color = DangerRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                when (step) {
                    1 -> {
                        // Step 1: Email Lookup
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("البريد الإلكتروني", color = TextMuted) },
                            singleLine = true,
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                val user = users.find { it.email.equals(emailInput.trim(), ignoreCase = true) }
                                if (user == null) {
                                    errorMessage = "لم يتم العثور على حساب بهذا البريد"
                                } else if (user.phone.isBlank()) {
                                    errorMessage = "لا يوجد رقم واتساب مسجَّل لهذا الحساب"
                                } else {
                                    targetUser = user
                                    generatedOtp = (100000..999999).random().toString()
                                    step = 2
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بحث عن الحساب", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    2 -> {
                        // Step 2: OTP Verification
                        val phone = targetUser?.phone ?: ""
                        val maskedPhone = if (phone.length >= 6) "${phone.take(4)}••••${phone.takeLast(2)}" else phone

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(InfoCyanContainer)
                                .border(1.dp, InfoCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱 تم إرسال كود التحقق لـ: $maskedPhone", color = InfoCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("(الكود للاختبار/التطوير: $generatedOtp)", fontSize = 11.sp, color = WarningGold)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6) otpInput = it },
                            label = { Text("كود التحقق (6 أرقام)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (otpInput == generatedOtp) {
                                    step = 3
                                } else {
                                    errorMessage = "كود التحقق غير صحيح"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحقق من الكود", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    3 -> {
                        // Step 3: New Password
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("كلمة المرور الجديدة", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confPass,
                            onValueChange = { confPass = it },
                            label = { Text("تأكيد كلمة المرور", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (newPass.length < 6) {
                                    errorMessage = "كلمة المرور لا تقل عن 6 أحرف"
                                } else if (newPass != confPass) {
                                    errorMessage = "كلمتا المرور غير متطابقتين"
                                } else {
                                    targetUser?.let { onPasswordResetSuccess(it, newPass) }
                                    step = 4
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تغيير كلمة المرور", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    4 -> {
                        // Step 4: Success
                        Text("✅ تم تغيير كلمة المرور بنجاح!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تسجيل الدخول الآن", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
