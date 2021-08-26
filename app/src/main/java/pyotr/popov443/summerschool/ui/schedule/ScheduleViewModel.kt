package pyotr.popov443.summerschool.ui.schedule

import androidx.lifecycle.ViewModel
import pyotr.popov443.summerschool.data.Repository

class ScheduleViewModel : ViewModel() {

    val events = Repository.getEvents()

    val groups = Repository.groups

    fun getGroup(pos: Int) = groups[pos]

}