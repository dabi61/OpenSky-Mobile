package com.dabi.opensky.core.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toUtcIsoString(): String {
    // millis lấy từ DatePicker là theo mốc UTC, nhưng bạn thường hiểu là ngày ở local.
    // Cách an toàn: map sang LocalDate theo zone máy → set 00:00 local → convert sang UTC.
    val zone = ZoneId.systemDefault()
    val localDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
    val startOfDayLocal = localDate.atStartOfDay(zone)
    return startOfDayLocal.toInstant().toString() // ISO_INSTANT, có 'Z'
}