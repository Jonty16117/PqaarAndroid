package com.pqaar.app.utils

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeConversions {
    @Throws(ParseException::class)
    fun TimestampToMillis(timestamp: String): Long {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        val date = sdf.parse(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        return calendar.timeInMillis
    }

    fun MillisToTimestamp(millis: Long): String {
        val simple: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        val result = Date(millis)
        return (simple.format(result)).toString()
    }

    fun CurrDateTimeInMillis(): Long {
        return TimestampToMillis(
            SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
                .format(Calendar.getInstance().time))
    }
}
