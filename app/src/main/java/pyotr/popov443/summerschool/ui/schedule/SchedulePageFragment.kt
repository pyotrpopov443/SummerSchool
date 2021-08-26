package pyotr.popov443.summerschool.ui.schedule

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("events", ArrayList<Parcelable>(events))
    }

    override fun onResume() {
        //this updates adapter
        if (binding.expandableSchedule.isGroupExpanded(0)) {
            binding.expandableSchedule.collapseGroup(0)
            binding.expandableSchedule.expandGroup(0)
        } else {
            binding.expandableSchedule.expandGroup(0)
            binding.expandableSchedule.collapseGroup(0)
        }
        super.onResume()
    }

}