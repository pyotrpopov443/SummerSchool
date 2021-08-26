package pyotr.popov443.summerschool.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import pyotr.popov443.summerschool.data.models.Event
import pyotr.popov443.summerschool.databinding.GroupScheduleBinding
import pyotr.popov443.summerschool.databinding.ItemScheduleBinding

class ExpandableScheduleAdapter(events: List<Event>) : BaseExpandableListAdapter() {

    private val schedule = linkedMapOf<String, MutableList<Event>>().apply {
        events.forEach {
            val date = it.dateString()
            put(date, (get(date) ?: mutableListOf()).apply {
                add(it)
            })
        }
    }

    private val days = mutableListOf<String>().apply {
        schedule.forEach {
            add(it.key)
        }
    }.distinct()

    override fun getGroupCount() = days.size

    override fun getChildrenCount(groupPosition: Int) = schedule[days[groupPosition]]!!.size

    override fun getGroup(groupPosition: Int) = schedule[days[groupPosition]]

    override fun getChild(groupPosition: Int, childPosition: Int) = schedule[days[groupPosition]]!![childPosition]

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun hasStableIds() = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = LayoutInflater.from(parent!!.context)
        val binding = GroupScheduleBinding.inflate(inflater, parent, false)
        binding.groupName.text = days[groupPosition]
        return binding.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val inflater = LayoutInflater.from(parent!!.context)
        val binding = ItemScheduleBinding.inflate(inflater, parent, false)
        binding.itemName.text = getChild(groupPosition, childPosition).toString()
        return binding.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

}