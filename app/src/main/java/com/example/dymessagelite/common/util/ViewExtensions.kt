package com.example.dymessagelite.common.util

import android.content.res.Resources
import android.util.TypedValue

fun Int.dpToPx(): Int{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
fun Int.pxToDp(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

fun Float.dpToPx(): Float{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
}
fun Float.pxToDp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}