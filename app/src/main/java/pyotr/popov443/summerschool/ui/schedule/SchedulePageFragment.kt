package pyotr.popov443.summerschool.ui.schedule

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import pyotr.popov443.summerschool.R
import pyotr.popov443.summerschool.data.models.Event
import pyotr.popov443.summerschool.databinding.FragmentSchedulePageBinding

class SchedulePageFragment(private var events: List<Event> = listOf()) : Fragment(R.layout.fragment_schedule_page) {

    private val binding by viewBinding(FragmentSchedulePageBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val savedList = savedInstanceState?.getParcelableArrayList<Event>("events")
        if (savedList != null) events = savedList
        binding.expandableSchedule.setAdapter(ExpandableScheduleAdapter(events))

        binding.expandableSchedule.setOnGroupClickListener { _, _, groupPosition, _ ->
            if (binding.expandableSchedule.isGroupExpanded(groupPosition))
                binding.expandableSchedule.collapseGroupWithAnimation(groupPosition)
            else
                binding.expandableSchedule.expandGroupWithAnimation(groupPosition)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("events", ArrayList<Parcelable>(events))
    }

    override fun onResume() {
        binding.expandableSchedule.requestLayout()
        super.onResume()
    }

}