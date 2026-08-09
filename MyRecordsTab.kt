package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Leave
import com.example.data.models.User
import com.example.ui.components.LeaveCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecordsTab(
    currentUser: User?,
    leaves: List<Leave>,
    onApproveLeave: (Leave) -> Unit,
    onRejectLeave: (Leave) -> Unit,
    onDeleteLeave: (Leave) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") } // "" (all), "pending", "approved", "rejected"
    var expandedStatusMenu by remember { mutableStateOf(false) }

    val userLeaves = remember(leaves, currentUser, searchQuery, selectedStatus) {
        val baseList = if (currentUser?.role == "admin") leaves else leaves.filter { it.empId == currentUser?.staffId }
        baseList.filter { leave ->
            val matchSearch = searchQuery.isBlank() || leave.empName.contains(searchQuery, ignoreCase = true)
            val matchStatus = selectedStatus.isBlank() || leave.status == selectedStatus
            matchSearch && matchStatus
        }.sortedByDescending { it.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("🔍 بحث بالاسم...", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard1,
                    unfocusedContainerColor = SurfaceCard1,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.weight(1f)
            )

            ExposedDropdownMenuBox(
                expanded = expandedStatusMenu,
                onExpandedChange = { expandedStatusMenu = !expandedStatusMenu }
            ) {
                val statusLabel = when (selectedStatus) {
                    "pending" -> "معلق"
                    "approved" -> "مقبول"
                    "rejected" -> "مرفوض"
                    else -> "الكل"
                }

                OutlinedTextField(
                    value = statusLabel,
                    onValueChange = {},
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard1,
                        unfocusedContainerColor = SurfaceCard1,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedStatusMenu,
                    onDismissRequest = { expandedStatusMenu = false },
                    modifier = Modifier.background(SurfaceCard2)
                ) {
                    DropdownMenuItem(
                        text = { Text("الكل", color = TextPrimary) },
                        onClick = { selectedStatus = ""; expandedStatusMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("معلق", color = WarningGold) },
                        onClick = { selectedStatus = "pending"; expandedStatusMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("مقبول", color = AccentGreen) },
                        onClick = { selectedStatus = "approved"; expandedStatusMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("مرفوض", color = DangerRed) },
                        onClick = { selectedStatus = "rejected"; expandedStatusMenu = false }
                    )
                }
            }
        }

        // List
        if (userLeaves.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "📋 لا توجد سجلات مطابقة للبحث",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(userLeaves) { leave ->
                    LeaveCard(
                        leave = leave,
                        isAdmin = currentUser?.role == "admin",
                        onApprove = { onApproveLeave(leave) },
                        onReject = { onRejectLeave(leave) },
                        onDelete = { onDeleteLeave(leave) }
                    )
                }
            }
        }
    }
}
