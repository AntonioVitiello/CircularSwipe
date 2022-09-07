package com.links.events.circular_swipe.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * Created by Antonio Vitiello on 02/01/2020.
 */
class ExtendTouchView : View {
    private var mTouchInterface: ITouchEvent? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        mTouchInterface?.onTouchEvent(event)
        return true
    }

    fun on(touchInterface: ITouchEvent) {
        mTouchInterface = touchInterface
    }

    interface ITouchEvent {
        fun onTouchEvent(event: MotionEvent): Boolean
    }

}
