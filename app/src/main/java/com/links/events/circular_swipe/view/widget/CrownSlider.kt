package com.links.events.circular_swipe.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.links.events.circular_swipe.R
import com.links.events.circular_swipe.view.widget.CrownSlider.TurnFactorType.TURN_FACTOR_15
import com.links.events.circular_swipe.view.widget.CrownSlider.TurnFactorType.TURN_FACTOR_60
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt


/**
 * Created by Antonio Vitiello on 02/01/2020.
 */
class CrownSlider : View, ExtendTouchView.ITouchEvent {

    private var mCurrPoint = PointF()
    private var mPrevPoint = PointF()
    private var mCircleCenter = PointF()

    private var mCurrentAngle = 0.0
    private var mPreviousAngle = 0.0
    private var mDiffAngleDegreeFromStart = 0.0
    private var mCrownAngleDegree = 0.0

    private var mDiffMinutes = 0

    private var mCurrentRadius = 0.0
    private var mPreviousRadius = 0.0

    private var mCrownImage: Drawable? = null
    private var mDegreeToMinuteFactor = DEGREE_TO_60_MINUTE_FACTOR

    private var mIsTouchStarted = false
    private var mListener: OnSliderListener? = null

    enum class TurnFactorType { TURN_FACTOR_60, TURN_FACTOR_15 }

    companion object {
        const val RADIANT_TO_DEGREE_FACTOR = 180 / Math.PI
        const val DEGREE_TO_60_MINUTE_FACTOR = 1.0 / 6.0
        const val DEGREE_TO_15_MINUTE_FACTOR = 1.0 / 24.0
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val attrib = context.obtainStyledAttributes(attrs, R.styleable.CrownSlider, defStyleAttr, 0)

        try {
            val crownImage = attrib.getDrawable(R.styleable.CrownSlider_crown_image)
            setCrownImage(crownImage)

            val turnFactorIndex = attrib.getInt(R.styleable.CrownSlider_crown_turn_factor, 0)
            val turnFactorType = TurnFactorType.values()[turnFactorIndex]
            setCrownTurnFactor(turnFactorType)
        } finally {
            attrib.recycle()
        }
    }

    fun setCrownImage(drawable: Drawable?) {
        mCrownImage = drawable
    }

    private fun setCrownTurnFactor(factor: TurnFactorType) {
        mDegreeToMinuteFactor = when (factor) {
            TURN_FACTOR_15 -> DEGREE_TO_15_MINUTE_FACTOR
            TURN_FACTOR_60 -> DEGREE_TO_60_MINUTE_FACTOR
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        val smallerDim = if (w > h) h else w

        val largestCenteredSquareLeft = (w - smallerDim) / 2f
        val largestCenteredSquareTop = (h - smallerDim) / 2f
        val largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim
        val largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim

        mCircleCenter.x = largestCenteredSquareRight / 2f + (w - largestCenteredSquareRight) / 2f
        mCircleCenter.y = largestCenteredSquareBottom / 2f + (h - largestCenteredSquareBottom) / 2f

        super.onSizeChanged(w, h, oldW, oldH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCrownImage?.apply {
            setBounds(0, 0, width, height)
            val rotated = getRotatedCrown(this, mCrownAngleDegree)
            rotated.draw(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { //on first touch
                parent.requestDisallowInterceptTouchEvent(true)
                setCurrentPointData(event.x, event.y)
                mIsTouchStarted = true
                mListener?.onSliderStart()
                mDiffAngleDegreeFromStart = 0.0
//                Log.d(TAG, "onSliderStart:$mDiffMinutes")
            }

            MotionEvent.ACTION_MOVE -> { //on moving crown
                if (mIsTouchStarted) {
                    saveCurrentPointData()
                    setCurrentPointData(event.x, event.y)
                    calculateMovement()
                    invalidate()
                    mListener?.onSliderChange(mDiffMinutes)
//                    Log.d(TAG, "onSliderChange:$mDiffMinutes")
                }
            }

            MotionEvent.ACTION_UP -> { //on stop touch
                parent.requestDisallowInterceptTouchEvent(false)
                mIsTouchStarted = false
                mListener?.onSliderStop()
//                Log.d(TAG, "onSliderStop:$mDiffMinutes")
            }

        }

        return true
    }

    private fun saveCurrentPointData() {
        mPreviousRadius = mCurrentRadius
        mPrevPoint.x = mCurrPoint.x
        mPrevPoint.y = mCurrPoint.y
        mPreviousAngle = mCurrentAngle
    }

    private fun setCurrentPointData(x: Float, y: Float) {
        mCurrentRadius = getDistanceBetween(x, y, mCircleCenter)
        mCurrPoint.x = x - mCircleCenter.x
        mCurrPoint.y = mCircleCenter.y - y
        mCurrentAngle = acos(mCurrPoint.x.toDouble() / mCurrentRadius)
    }

    private fun calculateMovement() {
        var diffAngle = (mPreviousAngle - mCurrentAngle)
        if (mCurrPoint.y < 0) {
            diffAngle = -diffAngle
        }
        val diffAngleDegree = diffAngle * RADIANT_TO_DEGREE_FACTOR
        mDiffAngleDegreeFromStart += diffAngleDegree
        mCrownAngleDegree = (mCrownAngleDegree + diffAngleDegree).rem(360)
        mDiffMinutes = (mDiffAngleDegreeFromStart * mDegreeToMinuteFactor).roundToInt()
    }

    private fun getRotatedCrown(drawable: Drawable, degree: Double): Drawable {
        return object : LayerDrawable(arrayOf(drawable)) {
            override fun draw(canvas: Canvas) {
                canvas.rotate(
                    degree.toFloat(),                        // degrees
                    (drawable.bounds.width() / 2).toFloat(), // px
                    (drawable.bounds.height() / 2).toFloat() // py
                )
                super.draw(canvas)
            }
        }
    }

    fun getDistanceBetween(x: Float, y: Float, p: PointF): Double {
        val dx = x - p.x
        val dy = y - p.y
        return sqrt(Math.pow(dx.toDouble(), 2.0) + Math.pow(dy.toDouble(), 2.0))
    }

    fun getDistanceBetween(p1: PointF, p2: PointF): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(Math.pow(dx.toDouble(), 2.0) + Math.pow(dy.toDouble(), 2.0))
    }

    fun getDirection(p2: PointF, p1: PointF): Int {
        if (p2.y >= 0 && p1.y >= 0) {
            return sign(p2.x - p1.x).toInt()
        } else if (p2.x >= 0 && p1.x >= 0) {
            return sign(p2.y - p1.y).toInt()
        } else if (p2.y < 0 && p1.y < 0) {
            return sign(p2.x - p1.x).toInt()
        } else if (p2.x < 0 && p1.x >= 0) {
            return sign(p2.y - p1.y).toInt()
        }
        return 0
    }

    fun setOnSliderMovedListener(listener: OnSliderListener) {
        mListener = listener
    }

    fun stopSliding() {
        mIsTouchStarted = false
        mCrownAngleDegree = 0.0
        invalidate()
    }

//    public fun reset() {
//        mDiffAngle = 0.0
//        mDiffAngleDegreeFromStart = 0.0
//        mDiffMinutes = 0
//        invalidate()
//    }

//    fun restart() {
//        mPreviousRadius = 0.0
//        mCurrentRadius = 0.0
//        mCurrentAngle = 0.0
//        mDiffAngle = 0.0
//        mDiffAngleDegreeFromStart = 0.0
//        mDiffMinutes = 0
//        mPreviousAngle = 0.0
//        mPrevPoint.x = 0f
//        mPrevPoint.y = 0f
//        mCurrPoint.x = 0f
//        mCurrPoint.y = 0f
//        invalidate()
//    }

//    fun interrupt(){
//        mIsTouchStarted = false
//    }

    interface OnSliderListener {

        fun onSliderStart() //on crown first touch

        fun onSliderChange(newValue: Int) //on crown rotated

        fun onSliderStop() //on crown last touch

//        fun onSliderData(
//                currPoint: PointF,
//                prevPoint: PointF,
//                circleCenter: PointF,
//                currentAngle: Double,
//                previousAngle: Double,
//                diffAngle: Double,
//                totAngle: Double,
//                currentRadius: Double,
//                previousRadius: Double,
//                diffMinutes: Int
//        )

    }

}
