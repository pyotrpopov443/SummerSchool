package pyotr.popov443.summerschool.data

import pyotr.popov443.summerschool.data.models.Event
import java.util.*

object Repository {

    fun getEvents() : List<Event> {
        return listOf(
            Event(listOf("Lawyers Russian", "Lawyers English", "Compliance"),
                datetime(25, 8, 2021, 13, 0), datetime(25, 8, 2021, 14, 30),
            "Royal Residence", "Lunch"),
            Event(listOf("Compliance"),
                datetime(25, 8, 2021, 14, 30), datetime(25, 8, 2021, 16, 0),
                "Skvorechnik", "Effective negotiations"),
            Event(listOf("Lawyers Russian"),
                datetime(26, 8, 2021, 9, 0), datetime(26, 8, 2021, 11, 30),
                "Aquarium", "How to write laws", link = "https://t.rasp.yandex.ru/search/suburban/?fromId=s9611997&fromName=Зеленоградск-новый&toId=s9623137&toName=Калининград-Южный&when=завтра")
        )
    }

    val groups = mutableListOf<String>().apply {
        getEvents().forEach {
            addAll(it.groups!!)
        }
    }.distinct()

    private fun datetime(d: Int, m: Int, y: Int, h: Int, min: Int) : Long {
        val calendar = Calendar.getInstance()
        calendar.set(y, m-1, d, h, min)
        return calendar.time.time
    }

}