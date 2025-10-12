package com.dabi.opensky.feature.room

import kotlin.math.round

fun Double.toVnd(): String = "%,d VND".format(round(this).toLong()).replace(',', '.')