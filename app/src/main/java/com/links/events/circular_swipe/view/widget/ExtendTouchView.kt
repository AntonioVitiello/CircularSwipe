package com.links.events.circular_swipe.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo


/**
 * Created by Antonio Vitiello on 02/01/2020.
 */
class ExtendTouchView : View {
    private var mTouchEvent: ITouchEvent? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        when (event.action) {
            AccessibilityNodeInfo.ACTION_CLICK -> performClick()
            else -> mTouchEvent?.onTouchEvent(event)
        }
        return true
    }

    fun of(touchEvent: ITouchEvent) {
        mTouchEvent = touchEvent
    }

    override fun performClick(): Boolean {
        super.performClick()
        return mTouchEvent?.let { touchEvent ->
            parent.requestDisallowInterceptTouchEvent(false)
            touchEvent.performClick()
        } ?: false
    }


    interface ITouchEvent {
        fun onTouchEvent(event: MotionEvent): Boolean
        fun performClick(): Boolean
    }

}
