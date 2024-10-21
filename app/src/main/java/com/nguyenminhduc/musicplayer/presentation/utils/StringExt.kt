package com.nguyenminhduc.musicplayer.presentation.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

fun Long.toMinuteAndSecond(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val sdf = SimpleDateFormat("mm:ss")
    return sdf.format(calendar.time)
}