package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Leave
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarStrip(
    leaves: List<Leave>,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayNameSdf = SimpleDateFormat("EEE", Locale("ar"))
    val today = Calendar.getInstance()

    val daysList = remember(leaves) {
        val list = mutableListOf<CalendarDayInfo>()
        val cal = Calendar.getInstance()

        for (i in 0 until 14) {
            val dateStr = sdf.format(cal.time)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
            val dowName = dayNameSdf.format(cal.time)

            val bookedLeave = leaves.find { l ->
                l.status != "rejected" && !isFriday && dateStr >= l.from && dateStr <= l.to
            }

            list.add(
                CalendarDayInfo(
                    dateStr = dateStr,
                    dayOfWeekName = dowName,
                    dayOfMonth = dayOfMonth,
                    isToday = i == 0,
                    isFriday = isFriday,
                    bookedName = bookedLeave?.empName?.substringBefore(" ") ?: ""
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
    ) {
        itemsIndexed(daysList) { _, day ->
            val bg = when {
                day.bookedName.isNotBlank() -> AccentGreenContainer
                day.isFriday -> DangerRedContainer.copy(alpha = 0.5f)
                else -> SurfaceCard2
            }

            val border = when {
                day.isToday -> PrimaryPurple
                day.bookedName.isNotBlank() -> AccentGreen
                day.isFriday -> DangerRed.copy(alpha = 0.3f)
                else -> BorderSubtle
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bg),
                shape = RoundedCornerShape(11.dp),
                modifier = Modifier
                    .width(54.dp)
                    .border(
                        width = if (day.isToday) 2.dp else 1.dp,
                        color = border,
                        shape = RoundedCornerShape(11.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeekName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (day.isFriday) DangerRed else TextPrimary
                    )
                    Text(
                        text = if (day.bookedName.isNotBlank()) day.bookedName else if (day.isFriday) "جمعة" else "",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (day.bookedName.isNotBlank()) AccentGreen else TextMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private data class CalendarDayInfo(
    val dateStr: String,
    val dayOfWeekName: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isFriday: Boolean,
    val bookedName: String
)
