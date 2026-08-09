package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StatGrid(
    pendingCount: Int,
    approvedCount: Int,
    staffCount: Int,
    upcomingCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                number = pendingCount.toString(),
                label = "إجازات معلقة",
                numColor = WarningGold,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                number = approvedCount.toString(),
                label = "مقبولة",
                numColor = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                number = staffCount.toString(),
                label = "الموظفين",
                numColor = PrimaryPurple,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                number = upcomingCount.toString(),
                label = "قادمة",
                numColor = PrimaryPurpleLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatBox(
    number: String,
    label: String,
    numColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard1),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = numColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        }
    }
}
