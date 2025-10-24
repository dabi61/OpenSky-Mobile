package com.dabi.opensky.feature.tour


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
private val zone: ZoneId = ZoneId.systemDefault()
@RequiresApi(Build.VERSION_CODES.O)
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
@RequiresApi(Build.VERSION_CODES.O)
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@RequiresApi(Build.VERSION_CODES.O)
fun parseIsoToZdt(iso: String): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.parse(iso), zone)

@RequiresApi(Build.VERSION_CODES.O)
fun fmtDate(zdt: ZonedDateTime): String = zdt.format(dateFormatter)
@RequiresApi(Build.VERSION_CODES.O)
fun fmtTime(zdt: ZonedDateTime): String = zdt.format(timeFormatter)

@RequiresApi(Build.VERSION_CODES.O)
fun durationLabel(startIso: String, endIso: String): String {
    val start = parseIsoToZdt(startIso)
    val end = parseIsoToZdt(endIso)
    val d = Duration.between(start, end)
    val days = d.toDays()
    val hours = d.minusDays(days).toHours()
    return when {
        days > 0 && hours > 0 -> "${days}d ${hours}h"
        days > 0 -> "${days}d"
        else -> "${hours}h"
    }
}



/** VD: 2025-10-24T18:12:16.024Z  */
@RequiresApi(Build.VERSION_CODES.O)
fun nowUtcZMillis(): String =
    Instant.now()
        .truncatedTo(ChronoUnit.MILLIS)   // đảm bảo 3 chữ số mili
        .toString()                       // ISO_INSTANT, auto hậu tố 'Z'

/** Convert bất kỳ chuỗi ISO-offset → UTC-Z (giữ mili nếu có) */
@RequiresApi(Build.VERSION_CODES.O)
fun toUtcZMillis(iso: String): String =
    OffsetDateTime.parse(iso)
        .toInstant()
        .truncatedTo(ChronoUnit.MILLIS)
        .toString()

/** Nếu bạn có LocalDateTime theo giờ máy, convert về UTC-Z */
@RequiresApi(Build.VERSION_CODES.O)
fun localToUtcZMillis(local: LocalDateTime): String =
    local.atZone(ZoneId.systemDefault())
        .toInstant()
        .truncatedTo(ChronoUnit.MILLIS)
        .toString()

/** Parse ISO (có thể Z hoặc có offset) về Instant để so sánh thời gian */
@RequiresApi(Build.VERSION_CODES.O)
fun parseInstant(iso: String): Instant =
    if (iso.endsWith("Z")) Instant.parse(iso)
    else OffsetDateTime.parse(iso).toInstant()

