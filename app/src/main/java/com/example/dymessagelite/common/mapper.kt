package com.example.dymessagelite.common

import android.icu.util.Calendar
import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.list.DisplayListItem
import com.example.dymessagelite.data.model.detail.MegDetailCell
import com.example.dymessagelite.data.model.list.MegEntity
import com.example.dymessagelite.data.model.list.MegItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun MegEntity.toMegItem(): MegItem {
    return MegItem(
        id = id,
        avatar = avatar,
        name = name,
        summary = latestMessage,
        timestamp = formatTimestampToString(timestamp),
        unreadCount = unreadCount,
        type = type,
        remark =remark
    )
}
fun ChatEntity.toMegDetailCell(): MegDetailCell {
    return MegDetailCell(
        id = this.id,
        content = this.content,
        timestamp = this.timestamp.toString(),
        isMine = this.isMine,
        type = type,
        isDisplay = isDisplay,
        isClick = isClick,
        isRead = isRead
    )
}
fun List<MegEntity>.toMegItems(): List<MegItem> {
    return this.map {
        it.toMegItem()
    }
}
fun List<ChatEntity>.toMegDetailCellList(): List<MegDetailCell> {
    return this.map {
        it.toMegDetailCell()
    }
}

fun MegItem.toDisplayListItem(type: Int): DisplayListItem{
    return DisplayListItem(
        id = this.id,
        avatar = this.avatar,
        name = this.name,
        context = this.summary,
        timestamp = this.timestamp,
        contentType = this.type,
        unreadCount = this.unreadCount,
        displayType = type,
        remark = remark
    )
}
fun List<MegItem>.toDisplayListItems(type: Int): List<DisplayListItem> {
    return this.map {
        it.toDisplayListItem(type)
    }
}
private fun formatTimestampToString(timestamp: Long): String{
    val currentTime = System.currentTimeMillis()
    val targetTime = timestamp
    val diff = currentTime - timestamp


    val targetCalendar =
        Calendar.getInstance().apply { timeInMillis = timestamp }
    val currentCalendar =
        Calendar.getInstance().apply { timeInMillis = currentTime }

    val oneMinute = 60 * 1000L
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour

    return when{
        diff < oneMinute -> "刚刚"
        diff < oneHour -> "${diff / oneMinute}分钟前"

        currentCalendar.get(Calendar.DAY_OF_YEAR) == targetCalendar.get(Calendar.DAY_OF_YEAR)
                && currentCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(targetTime))
        }

        // 4. 昨天 -> 昨天 HH:mm (例如：昨天 09:15)
        currentCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR)
                && currentCalendar.get(Calendar.DAY_OF_YEAR) == targetCalendar.get(Calendar.DAY_OF_YEAR) + 1 -> {
            "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).
            format(Date(targetTime))
        }

        diff < 7 * oneDay -> "${diff / oneDay}天前"

        // 6. 其他 -> MM-dd (例如：03-15)
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).
        format(Date(targetTime))
    }

}
