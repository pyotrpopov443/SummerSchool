package pyotr.popov443.summerschool.ui.schedule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import pyotr.popov443.summerschool.R
import pyotr.popov443.summerschool.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private val scheduleViewModel: ScheduleViewModel by viewModels()

    private val binding by viewBinding(FragmentScheduleBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.schedulePager.adapter = SchedulePagerAdapter(this,
            scheduleViewModel.events, scheduleViewModel.groups)
        TabLayoutMediator(binding.tabs, binding.schedulePager) { tab, position ->
            tab.text = scheduleViewModel.getGroup(position)
        }.attach()
    }

}