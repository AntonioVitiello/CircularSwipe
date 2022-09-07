package com.links.events.circular_swipe.view.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.text.Spanned
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.links.events.circular_swipe.BuildConfig
import com.links.events.circular_swipe.R
import com.links.events.circular_swipe.view.tools.Utils.Companion.fullTimeFormat
import com.links.events.circular_swipe.view.tools.Utils.Companion.iso8601FullDateFormat
import com.links.events.circular_swipe.view.tools.Utils.Companion.iso8601ShortDateFormat
import com.links.events.circular_swipe.view.tools.Utils.Companion.shortDateFormat
import com.links.events.circular_swipe.view.tools.Utils.Companion.shortTimeFormat
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val FORMAT_DATE_DD_MM_YYYY = "dd/MM/yyyy"
const val FORMAT_DISTANCE_ZERO_DECIMAL = "###"
const val FORMAT_DISTANCE_ONE_DECIMAL = "###.#"
const val FORMAT_DISTANCE_TWO_DECIMAL = "###.##"
const val FORMAT_SHORT_DATE = "dd/MM/yyyy"
const val FORMAT_FULL_DATE = "dd/MM/yyyy HH:mm"
const val FORMAT_PAYMENT_FILTER = "dd/MM/yyyy"
const val FORMAT_DATE_FULL = "dd/MM/yyyy HH:mm"
const val FORMAT_HOUR_MINUTE = "HH:mm"
const val FORMAT_ACTIVITIES_DATE_TIME = "yyyy-MM-dd HH:mm:ss"
const val FORMAT_ISO8601_FULL_DATE = "yyyy-MM-dd'T'HH:mm:ss'Z'"
const val FORMAT_ISO8601_SHORT_DATE = "yyyy-MM-dd"
const val FORMAT_SHORT_TIME = "HH:mm"
const val FORMAT_BLUE_LINES_PARKING_DATE = "HH:mm - dd/MM/yyyy"
const val FORMAT_FULL_TIME = "HH:mm:ss"

class Utils {

    companion object {
        val iso8601ShortDateFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_ISO8601_SHORT_DATE, Locale.getDefault())
        }
        val iso8601FullDateFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_ISO8601_FULL_DATE, Locale.getDefault())
        }
        val fullDateFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_FULL_DATE, Locale.getDefault())
        }
        val shortDateFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_SHORT_DATE, Locale.getDefault())
        }
        val shortTimeFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_SHORT_TIME, Locale.getDefault())
        }
        val fullTimeFormat: SimpleDateFormat by lazy(LazyThreadSafetyMode.NONE) {
            SimpleDateFormat(FORMAT_FULL_TIME, Locale.getDefault())
        }
        val twoDecimalFormater: NumberFormat by lazy {
            NumberFormat.getNumberInstance().apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
        }
    }
}

fun dateToString(millis: Long, dateFormat: String): String {
    val format = SimpleDateFormat(dateFormat, Locale.getDefault())
    return format.format(Date(millis))
}

fun dateStringToLong(dateString: String?, dateFormat: String): Long? {
    if (dateString != null) {
        try {
            val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = sdf.parse(dateString)
            return date?.time
        } catch (exc: Exception) {
            logError("Bad format $dateString", exc)
        }
    }
    return null
}

fun logError(tag: String, t: Throwable) {
    logError(tag, t.message, t)
}

fun logError(tag: String, message: String?, t: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message ?: t.message ?: "", t)
    }
}

fun logError(tag: String, message: String) {
    val t = Throwable(message)
    logError(tag, t)
}

fun log(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}

fun showFragment(supportFragmentManager: FragmentManager, containerId: Int, fragment: Fragment, tag: String, addToBackStack: Boolean) {
    val name: String? = if (addToBackStack) tag else null
    val transaction = supportFragmentManager.beginTransaction()
//    transaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    supportFragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    transaction.replace(containerId, fragment, tag).addToBackStack(name).commit()
}

fun showProgress(show: Boolean?, progressView: View) {
    try {
        show?.let {
            progressView.visibility = if (it) View.VISIBLE else View.GONE
        }
    } catch (t: Throwable) {
        logError("showProgress", t)
    }
}

fun openPlayStore(act: Activity, packageName: String) {

    try {
        runActionViewIntent("market://details?id=$packageName", act)
    } catch (t: Throwable) {
        runActionViewIntent("https://play.google.com/store/apps/details?id=$packageName", act)
    }
}

fun runActionViewIntent(url: String, act: Activity? = null) {
    val uri = Uri.parse(url)
    act?.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

fun formatDistanceInKm(distanceInMeter: Float?, format: String): String? {
    distanceInMeter?.also {
        val df = DecimalFormat(format)
        return df.format(distanceInMeter.div(1000))
    }
    return null
}

fun formatParkingReservationTime(millisUntilFinished: Long): String {
    return String.format(
        "%02d : %02d",
        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(
                millisUntilFinished
            )
        )
    )
}

fun callPhone(act: Activity, phone: String?) {
    if (phone?.isNotEmpty() == true) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        act.startActivity(intent)
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun openGoogleMapsAndNavigateToLocation(act: Activity, latitude: Double?, longitude: Double?) {

    val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    mapIntent.`package` = "com.google.android.apps.maps"

    if (mapIntent.resolveActivity(act.packageManager) != null) {
        act.startActivity(mapIntent)
    }
}


fun callNumberAction(act: Activity, number: String?) {
    number?.let {
        try {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
            act.startActivity(callIntent)
        } catch (t: Throwable) {
            logError("Utils:callNumberAction", t)
        }
    }

}

fun formatAmountCents(amtCents: String?, currency: Currency = Currency.getInstance("EUR"), noCurrency: Boolean = false): String {
    amtCents ?: return "--"
    var amtCentsPad = "000"
    if (amtCents.length == 1) {
        amtCentsPad = "00$amtCents"
    } else if (amtCents.length == 2) {
        amtCentsPad = "0$amtCents"
    } else if (amtCents.length > 2) {
        amtCentsPad = amtCents
    }
    val intPart = amtCentsPad.substring(0, amtCentsPad.length - 2)
    val decimalPart = amtCentsPad.substring(amtCentsPad.length - 2)
    if (noCurrency) {
        return "${addThousansComma(intPart)},$decimalPart"
    } else {
        return "${addThousansComma(intPart)},$decimalPart ${currency.symbol}"
    }
}

fun addThousansComma(intPart: String): String {
    if (intPart.length <= 3) {
        return intPart
    }
    return addThousansComma(intPart.substring(0, intPart.length - 3)) + "." + intPart.substring(intPart.length - 3)
}

fun formatAmountMoneyTwoDecimal(value: Double): String? {
    val df = DecimalFormat(FORMAT_DISTANCE_TWO_DECIMAL)
    return df.format(value)
}

fun parseHtmlString(html: String): Spanned {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun timeStringFromMillisCancellation(millis: Long): String {
    return if ((TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)).toInt() != 0) {
        String.format(
            "%02d minuti",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
        )
    } else {
        String.format(
            "%02d secondi",
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
    }
}

fun getHourAndDateFromString(date: String?): Pair<String, String> {
    val sdf = SimpleDateFormat(FORMAT_ACTIVITIES_DATE_TIME, Locale.getDefault())
    val sdfDate = SimpleDateFormat(FORMAT_DATE_DD_MM_YYYY, Locale.getDefault())
    val sdfHour = SimpleDateFormat(FORMAT_HOUR_MINUTE, Locale.getDefault())
    var dateOut = "--"
    var hourOut = "--"
    if (date != null) {
        try {
            sdf.parse(date)?.let {
                dateOut = sdfDate.format(it)
                hourOut = sdfHour.format(it)
            }
        } catch (e: Exception) {
            //
        }
    }
    return Pair(dateOut, hourOut)
}

fun parseDateFromString(date: String?): Date? {
    val sdf = SimpleDateFormat(FORMAT_ACTIVITIES_DATE_TIME, Locale.getDefault())
    return date?.let { sdf.parse(it) }
}

fun formatDistanceInKmOrMeter(distanceInMeter: Float?, formatKm: String, formatMt: String): String? {
    distanceInMeter?.also {
        return if (distanceInMeter < 1000) {
            val df = DecimalFormat(formatMt)
            "${df.format(distanceInMeter)} m"
        } else {
            val df = DecimalFormat(formatKm)
            "${df.format(distanceInMeter.div(1000))} km"
        }
    }
    return null
}

fun setCheckboxView(
    checkBox: AppCompatCheckBox,
    isEnabled: Boolean,
    textColorEnabled: Int = R.color.white,
    textColorDisabled: Int = R.color.white_50,
    buttonColorEnabled: Int = R.color.white,
    buttonColorDisabled: Int = R.color.white_50
) {
    checkBox.isEnabled = isEnabled
    val textColor = ContextCompat.getColor(checkBox.context, if (isEnabled) textColorEnabled else textColorDisabled)
    checkBox.setTextColor(textColor)
    val buttonColor = ContextCompat.getColor(checkBox.context, if (isEnabled) buttonColorEnabled else buttonColorDisabled)
    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(buttonColor))
}

fun formatDate(date: Date?, placeholder: String = "--", dateFormat: DateFormat = shortDateFormat): String {
    return if (date == null) {
        placeholder
    } else {
        dateFormat.format(date)
    }
}

fun formatDate(timeMillis: Long?, placeholder: String = "--", dateFormat: DateFormat = shortDateFormat): String {
    return timeMillis?.let { formatDate(Date(timeMillis), placeholder, dateFormat) } ?: placeholder
}

fun parseDate(dateString: String?, dateFormat: DateFormat = shortDateFormat): Date? {
    return if (dateString == null) {
        null
    } else {
        try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

fun parseIso8601Date(dateString: String?): Date? {
    return parseDate(dateString, iso8601ShortDateFormat)
}

fun parseIso8601DateTime(dateString: String?): Date? {
    return parseDate(dateString, iso8601FullDateFormat)
}

fun parseFullTime(timeString: String?) = parseDate(timeString, fullTimeFormat)
fun parseShortTime(timeString: String?) = parseDate(timeString, shortTimeFormat)
fun formatShortDate(date: Date? = null) = formatDate(date ?: Date(), dateFormat = shortDateFormat)
fun formatShortTime(date: Date? = null) = formatDate(date ?: Date(), dateFormat = shortTimeFormat)

fun formatShortDateMillis(millis: Long? = null) = formatDate(millis?.let { Date(millis) }
    ?: Date(), dateFormat = shortDateFormat)

fun formatShortTimeMillis(millis: Long? = null) = formatDate(millis?.let { Date(millis) }
    ?: Date(), dateFormat = shortTimeFormat)

fun formatFullTimeMillis(millis: Long? = null) = formatDate(millis?.let { Date(millis) }
    ?: Date(), dateFormat = fullTimeFormat)

fun isNotEmpty(aString: CharSequence?): Boolean {
    return aString?.let { it.length > 0 } ?: false
}
