package com.dabi.opensky.feature.schedule


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dabi.opensky.core.model.schedule.ScheduleItem
import com.dabi.opensky.feature.tour.durationLabel
import com.dabi.opensky.feature.tour.fmtDate
import com.dabi.opensky.feature.tour.fmtTime
import com.dabi.opensky.feature.tour.parseIsoToZdt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleCard(
    item: ScheduleItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val start = parseIsoToZdt(item.startTime)
    val end = parseIsoToZdt(item.endTime)
    val max = item.tour?.maxPeople ?: 0
    val booked = item.numberPeople
    val remain = (max - booked).coerceAtLeast(0)
    val occupancy = if (max > 0) booked.toFloat() / max else 0f

    ElevatedCard(
        onClick = { onClick?.invoke() },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.tourName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusChip(item.status)
            }

            Text("${fmtDate(start)} ${fmtTime(start)} → ${fmtDate(end)} ${fmtTime(end)}  •  ${durationLabel(item.startTime, item.endTime)}",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Occupancy bar
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(occupancy)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text("Đã đăng ký: $booked/$max • Còn trống: $remain",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.user?.fullName != null) {
                AssistChip(onClick = {}, enabled = false, label = { Text("Người đặt: ${item.user.fullName}") })
            }
        }
    }
}

@Composable private fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "active" -> MaterialTheme.colorScheme.primary
        "completed", "done" -> MaterialTheme.colorScheme.tertiary
        "cancelled", "canceled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    AssistChip(onClick = {}, enabled = false, label = { Text(status) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = color
        )
    )
}
