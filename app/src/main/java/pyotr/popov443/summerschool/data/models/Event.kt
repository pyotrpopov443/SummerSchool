package pyotr.popov443.summerschool.data.models

import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(val groups: List<String>? = listOf(),
                 val start: Long? = null,
                 val end: Long? = null,
                 val place: String? = null,
                 val name: String? = null,
                 val professor: String? = null,
                 val info: String? = null,
                 val link: String? = null) : Parcelable {

    override fun toString(): String {
        var eventString = ""
        if (start != null && end != null)
            eventString += "\n${startString()} - ${endString()}"
        if (place != null)
            eventString += "\n$place"
        if (name != null)
            eventString += "\n$name"
        if (professor != null)
            eventString += "\n$professor"
        if (info != null)
            eventString += "\n$info"
        if (link != null)
            eventString += "\n$link"
        return eventString.drop(1)
    }

    fun dateString() = dateOf(start!!)

    fun startString() = timeOf(start!!)

    fun endString() = timeOf(end!!)

    private fun dateOf(time: Long) : String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val dayOfWeek = calendar.getDisplayName(
            Calendar.DAY_OF_WEEK,
            Calendar.LONG,
            Locale.getDefault()
        )
        return "$day $month, $dayOfWeek"
    }

    private fun timeOf(time: Long) : String {
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val hour = "0${calendar.get(Calendar.HOUR_OF_DAY)}".takeLast(2)
        val minute = "0${calendar.get(Calendar.MINUTE)}".takeLast(2)
        return "$hour:$minute"
    }

}