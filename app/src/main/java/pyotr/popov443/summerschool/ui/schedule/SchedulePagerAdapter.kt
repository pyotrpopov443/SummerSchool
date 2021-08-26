package pyotr.popov443.summerschool.ui.schedule

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import pyotr.popov443.summerschool.data.models.Event

class SchedulePagerAdapter(fragment: Fragment,
                           private val events: List<Event>,
                           private val groups: List<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = groups.size

    override fun createFragment(position: Int) = SchedulePageFragment(scheduleOf(groups[position]))

    private fun scheduleOf(group: String) = mutableListOf<Event>().apply {
        events.forEach {
            if (it.groups!!.contains(group)) add(it)
        }
    }

}