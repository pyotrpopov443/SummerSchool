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

    val animationDuration = 300

    private var adapter: AnimatedExpandableListAdapter? = null

    override fun setAdapter(adapter: ExpandableListAdapter) {
        super.setAdapter(adapter)
        if (adapter is AnimatedExpandableListAdapter) {
            this.adapter = adapter
            this.adapter!!.setParent(this)
        } else {
            throw ClassCastException("$adapter must implement AnimatedExpandableListAdapter")
        }
    }

    fun expandGroupWithAnimation(groupPos: Int): Boolean {
        val groupFlatPos = getFlatListPosition(getPackedPositionForGroup(groupPos))
        if (groupFlatPos != -1) {
            val childIndex = groupFlatPos - firstVisiblePosition
            if (childIndex < childCount) {
                val v = getChildAt(childIndex)
                if (v.bottom >= bottom) {
                    adapter!!.notifyGroupExpanded(groupPos)
                    return expandGroup(groupPos)
                }
            }
        }
        adapter!!.startExpandAnimation(groupPos)
        return expandGroup(groupPos)
    }

    fun collapseGroupWithAnimation(groupPos: Int): Boolean {
        val groupFlatPos = getFlatListPosition(getPackedPositionForGroup(groupPos))
        if (groupFlatPos != -1) {
            val childIndex = groupFlatPos - firstVisiblePosition
            if (childIndex in 0 until childCount) {
                val v = getChildAt(childIndex)
                if (v.bottom >= bottom) {
                    return collapseGroup(groupPos)
                }
            } else {
                return collapseGroup(groupPos)
            }
        }
        val packedPos = getExpandableListPosition(firstVisiblePosition)
        var firstChildPos = getPackedPositionChild(packedPos)
        val firstGroupPos = getPackedPositionGroup(packedPos)
        firstChildPos = if (firstChildPos == -1 || firstGroupPos != groupPos) 0 else firstChildPos
        adapter!!.startCollapseAnimation(groupPos, firstChildPos)
        adapter!!.notifyDataSetChanged()
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
        fun setParent(parent: AnimatedExpandableListView) {
            this.parent = parent
        }

        abstract fun getRealChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View

        abstract fun getRealChildrenCount(groupPosition: Int): Int
        private fun getGroupInfo(groupPosition: Int): GroupInfo {
            var info = groupInfo[groupPosition]
            if (info == null) {
                info = GroupInfo()
                groupInfo.put(groupPosition, info)
            }
            return info
        }

        fun notifyGroupExpanded(groupPosition: Int) {
            val info = getGroupInfo(groupPosition)
            info.dummyHeight = -1
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
            val info = getGroupInfo(groupPosition)
            info.animating = false
        }

        override fun getChildType(groupPosition: Int, childPosition: Int): Int {
            val info = getGroupInfo(groupPosition)
            return if (info.animating) {
                0
            } else {
                1
            }
        }

        override fun getChildTypeCount(): Int {
            return 2
        }

        protected fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
            return LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0
            )
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            var convertV = convertView
            val info = getGroupInfo(groupPosition)
            return if (info.animating) {
                if (convertV !is DummyView) {
                    convertV = DummyView(parent?.context)
                    convertV.setLayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, 0))
                }
                if (childPosition < info.firstChildPosition) {
                    convertV.getLayoutParams().height = 0
                    return convertV
                }
                val listView = parent as ExpandableListView
                val dummyView = convertV
                dummyView.clearViews()
                dummyView.setDivider(
                    listView.divider,
                    parent.getMeasuredWidth(),
                    listView.dividerHeight
                )
                val measureSpecW =
                    MeasureSpec.makeMeasureSpec(parent.getWidth(), MeasureSpec.EXACTLY)
                val measureSpecH = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                var totalHeight = 0
                val clipHeight = parent.getHeight()
                val len = getRealChildrenCount(groupPosition)
                for (i in info.firstChildPosition until len) {
                    val childView = getRealChildView(groupPosition, i, i == len - 1, null, parent)
                    var p = childView.layoutParams as LayoutParams
                    if (p == null) {
                        p = generateDefaultLayoutParams() as LayoutParams
                        childView.layoutParams = p
                    }
                    val lpHeight = p.height
                    var childHeightSpec: Int
                    childHeightSpec = if (lpHeight > 0) {
                        MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY)
                    } else {
                        measureSpecH
                    }
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
                val state = if (dummyView.tag.also { o = it } == null) STATE_IDLE else (o as Int?)!!
                if (info.expanding && state != STATE_EXPANDING) {
                    val ani = ExpandAnimation(dummyView, 0, totalHeight, info)
                    ani.duration = this.parent!!.animationDuration.toLong()
                    ani.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation) {
                            stopAnimation(groupPosition)
                            notifyDataSetChanged()
                            dummyView.tag = STATE_IDLE
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationStart(animation: Animation) {}
                    })
                    dummyView.startAnimation(ani)
                    dummyView.tag = STATE_EXPANDING
                } else if (!info.expanding && state != STATE_COLLAPSING) {
                    if (info.dummyHeight == -1) {
                        info.dummyHeight = totalHeight
                    }
                    val ani = ExpandAnimation(dummyView, info.dummyHeight, 0, info)
                    ani.duration = this.parent!!.animationDuration.toLong()
                    ani.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation) {
                            stopAnimation(groupPosition)
                            listView.collapseGroup(groupPosition)
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
                convertV
            } else {
                getRealChildView(groupPosition, childPosition, isLastChild, convertV, parent)
            }
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            val info = getGroupInfo(groupPosition)
            return if (info.animating) {
                info.firstChildPosition + 1
            } else {
                getRealChildrenCount(groupPosition)
            }
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
            if (divider != null) {
                this.divider = divider
                this.dividerWidth = dividerWidth
                this.dividerHeight = dividerHeight
                divider.setBounds(0, 0, dividerWidth, dividerHeight)
            }
        }

        fun addFakeView(childView: View) {
            childView.layout(0, 0, width, childView.measuredHeight)
            views.add(childView)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            val len = views.size
            for (i in 0 until len) {
                val v = views[i]
                v.layout(left, top, left + v.measuredWidth, top + v.measuredHeight)
            }
        }

        fun clearViews() {
            views.clear()
        }

        public override fun dispatchDraw(canvas: Canvas) {
            canvas.save()
            if (divider != null) {
                divider!!.setBounds(0, 0, dividerWidth, dividerHeight)
            }
            val len = views.size
            for (i in 0 until len) {
                val v = views[i]
                canvas.save()
                canvas.clipRect(0, 0, width, v.measuredHeight)
                v.draw(canvas)
                canvas.restore()
                if (divider != null) {
                    divider!!.draw(canvas)
                    canvas.translate(0f, dividerHeight.toFloat())
                }
                canvas.translate(0f, v.measuredHeight.toFloat())
            }
            canvas.restore()
        }
    }

    private class ExpandAnimation (
        v: View,
        private val baseHeight: Int,
        endHeight: Int,
        info: GroupInfo
    ) :
        Animation() {
        private val delta: Int
        private val view: View
        private val groupInfo: GroupInfo
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            val `val`: Int
            `val` = if (interpolatedTime < 1.0f) {
                baseHeight + (delta * interpolatedTime).toInt()
            } else {
                baseHeight + delta
            }
            view.layoutParams.height = `val`
            groupInfo.dummyHeight = `val`
            view.requestLayout()
        }

        init {
            delta = endHeight - baseHeight
            view = v
            groupInfo = info
            view.layoutParams.height = baseHeight
            view.requestLayout()
        }
    }

}