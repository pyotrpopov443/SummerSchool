package pyotr.popov443.summerschool

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import java.util.*

class AnimatedExpandableListView(context: Context, attrs: AttributeSet) :
    ExpandableListView(context, attrs) {

    private var adapter: AnimatedExpandableListAdapter? = null

    override fun setAdapter(adapter: ExpandableListAdapter) {
        super.setAdapter(adapter)
        if (adapter is AnimatedExpandableListAdapter) this.adapter = adapter.setParent(this)
        else throw ClassCastException("$adapter must implement AnimatedExpandableListAdapter")
    }

    fun expandGroupWithAnimation(groupPos: Int): Boolean {
        val groupFlatPos = getFlatListPosition(getPackedPositionForGroup(groupPos))
        if (groupFlatPos != -1) {
            val childIndex = groupFlatPos - firstVisiblePosition
            if (childIndex < childCount && getChildAt(childIndex).bottom >= bottom) {
                adapter?.notifyGroupExpanded(groupPos)
                return expandGroup(groupPos)
            }
        }
        adapter?.startExpandAnimation(groupPos)
        return expandGroup(groupPos)
    }

    fun collapseGroupWithAnimation(groupPos: Int): Boolean {
        val groupFlatPos = getFlatListPosition(getPackedPositionForGroup(groupPos))
        if (groupFlatPos != -1) {
            val childIndex = groupFlatPos - firstVisiblePosition
            if (childIndex in 0 until childCount) {
                if (getChildAt(childIndex).bottom >= bottom) return collapseGroup(groupPos)
            } else return collapseGroup(groupPos)
        }
        val packedPos = getExpandableListPosition(firstVisiblePosition)
        var firstChildPos = getPackedPositionChild(packedPos)
        val firstGroupPos = getPackedPositionGroup(packedPos)
        firstChildPos = if (firstChildPos == -1 || firstGroupPos != groupPos) 0 else firstChildPos
        adapter?.startCollapseAnimation(groupPos, firstChildPos)
        adapter?.notifyDataSetChanged()
        return isGroupExpanded(groupPos)
    }

    private class GroupInfo {
        var animating = false
        var expanding = false
        var firstChildPosition = 0
        var dummyHeight = -1
    }

    abstract class AnimatedExpandableListAdapter : BaseExpandableListAdapter() {
        private val groupInfo = SparseArray<GroupInfo>()
        private var parent: AnimatedExpandableListView? = null

        fun setParent(parent: AnimatedExpandableListView) = this.also { it.parent = parent }

        abstract fun getRealChildView(groupPos: Int, childPos: Int, isLastChild: Boolean,
                                      convertView: View?, parent: ViewGroup?): View
        abstract fun getRealChildrenCount(groupPosition: Int): Int

        private fun getGroupInfo(groupPos: Int) =
                groupInfo[groupPos] ?: GroupInfo().also { groupInfo.put(groupPos, it) }

        fun notifyGroupExpanded(groupPosition: Int) {
            getGroupInfo(groupPosition).dummyHeight = -1
        }

        fun startExpandAnimation(groupPosition: Int) {
            val info = getGroupInfo(groupPosition)
            info.animating = true
            info.firstChildPosition = 0
            info.expanding = true
        }

        fun startCollapseAnimation(groupPosition: Int, firstChildPosition: Int) {
            val info = getGroupInfo(groupPosition)
            info.animating = true
            info.firstChildPosition = firstChildPosition
            info.expanding = false
        }

        private fun stopAnimation(groupPosition: Int) {
            getGroupInfo(groupPosition).animating = false
        }

        override fun getChildType(groupPosition: Int, childPosition: Int) =
                if (getGroupInfo(groupPosition).animating) 0 else 1

        override fun getChildTypeCount() = 2

        override fun getChildView(groupPos: Int, childPos: Int, isLastChild: Boolean,
                                  convertView: View?, parent: ViewGroup?): View {
            var convertV = convertView
            val info = getGroupInfo(groupPos)
            if (!info.animating) return getRealChildView(groupPos, childPos, isLastChild, convertV, parent)
            if (convertV !is DummyView) {
                convertV = DummyView(parent?.context)
                convertV.setLayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, 0))
            }
            if (childPos < info.firstChildPosition) return convertV.also { it.layoutParams.height = 0 }
            val listView = parent as ExpandableListView
            val dummyView = convertV.clearViews()
            dummyView.setDivider(listView.divider, parent.measuredWidth, listView.dividerHeight)
            val measureSpecW = MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.EXACTLY)
            val measureSpecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            var totalHeight = 0
            val clipHeight = parent.height
            val len = getRealChildrenCount(groupPos)
            for (i in info.firstChildPosition until len) {
                val childView = getRealChildView(groupPos, i, i == len - 1, null, parent)
                val p = childView.layoutParams as LayoutParams
                val lph = p.height
                val childHeightSpec = if (lph > 0) MeasureSpec.makeMeasureSpec(lph, MeasureSpec.EXACTLY)
                else measureSpecH
                childView.measure(measureSpecW, childHeightSpec)
                totalHeight += childView.measuredHeight
                dummyView.addFakeView(childView)
                if (totalHeight >= clipHeight) {
                    val averageHeight = totalHeight / (i + 1)
                    totalHeight += (len - i - 1) * averageHeight
                    break
                }
            }
            var o: Any?
            val state = if (dummyView.tag.also { o = it } == null) STATE_IDLE else o as Int
            if (info.expanding && state != STATE_EXPANDING) {
                val ani = ExpandAnimation(dummyView, 0, totalHeight, info)
                ani.duration = 300
                ani.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        stopAnimation(groupPos)
                        notifyDataSetChanged()
                        dummyView.tag = STATE_IDLE
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationStart(animation: Animation) {}
                })
                dummyView.startAnimation(ani)
                dummyView.tag = STATE_EXPANDING
            } else if (!info.expanding && state != STATE_COLLAPSING) {
                if (info.dummyHeight == -1) info.dummyHeight = totalHeight
                val ani = ExpandAnimation(dummyView, info.dummyHeight, 0, info)
                ani.duration = 300
                ani.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        stopAnimation(groupPos)
                        listView.collapseGroup(groupPos)
                        notifyDataSetChanged()
                        info.dummyHeight = -1
                        dummyView.tag = STATE_IDLE
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationStart(animation: Animation) {}
                })
                dummyView.startAnimation(ani)
                dummyView.tag = STATE_COLLAPSING
            }
            return convertV
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            val info = getGroupInfo(groupPosition)
            return if (info.animating) info.firstChildPosition + 1
            else getRealChildrenCount(groupPosition)
        }

        companion object {
            private const val STATE_IDLE = 0
            private const val STATE_EXPANDING = 1
            private const val STATE_COLLAPSING = 2
        }
    }

    private class DummyView(context: Context?) : View(context) {
        private val views: MutableList<View> = ArrayList()
        private var divider: Drawable? = null
        private var dividerWidth = 0
        private var dividerHeight = 0

        fun setDivider(divider: Drawable?, dividerWidth: Int, dividerHeight: Int) {
            divider?.setBounds(0, 0, dividerWidth, dividerHeight) ?: return
            this.divider = divider
            this.dividerWidth = dividerWidth
            this.dividerHeight = dividerHeight
        }

        fun addFakeView(childView: View) {
            childView.layout(0, 0, width, childView.measuredHeight)
            views.add(childView)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            for (i in 0 until views.size)
                views[i].apply { layout(left, top, left + measuredWidth, top + measuredHeight) }
        }

        fun clearViews() = this.also { it.views.clear() }

        public override fun dispatchDraw(canvas: Canvas) {
            canvas.save()
            divider?.setBounds(0, 0, dividerWidth, dividerHeight)
            val len = views.size
            for (i in 0 until len) {
                val v = views[i]
                canvas.save()
                canvas.clipRect(0, 0, width, v.measuredHeight)
                v.draw(canvas)
                canvas.restore()
                divider?.draw(canvas)
                canvas.translate(0f, v.measuredHeight.toFloat() + dividerHeight.toFloat())
            }
            canvas.restore()
        }
    }

    private class ExpandAnimation (val view: View, val baseHeight: Int,
                                   endHeight: Int, val groupInfo: GroupInfo) : Animation() {

        private val delta = endHeight - baseHeight

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            val h = if (interpolatedTime < 1f) baseHeight + (delta * interpolatedTime).toInt()
            else baseHeight + delta
            view.layoutParams.height = h
            groupInfo.dummyHeight = h
            view.requestLayout()
        }

        init {
            view.layoutParams.height = baseHeight
            view.requestLayout()
        }
    }
}