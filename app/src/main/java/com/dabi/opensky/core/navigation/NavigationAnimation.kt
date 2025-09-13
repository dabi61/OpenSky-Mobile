package com.dabi.opensky.core.navigation


import android.graphics.Rect
import androidx.compose.animation.core.tween

val boundsTransform = { _: Rect, _: Rect -> tween<Rect>(300) }
