package com.wj.view.scale

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import com.wj.view.utils.dp
import kotlin.math.absoluteValue


/**
 * @author WuJia
 *
 * @date 2023/8/18
 * @description
 */
class ScaleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs) {

    // 长刻度的默认高度
    private var longScaleHeight = 50.dp

    // 短刻度的默认高度
    private var shortScaleHeight = 40.dp

    // 刻度的宽度
    private var scaleWidth = 1.5f.dp

    // 刻度尺的刻度间隔
    private var scaleSpace = 11.dp
    private var scaleColor = Color.BLACK

    // 刻度尺的最大值、最小值
    private var scaleMaxValue = 155
    private var scaleMinValue = 0

    // 刻度尺的刻度数量
    private var scaleCount = 0

    // 表尺中间标记的高度
    private val pointHeight = 70.dp
    private val pointWidth = 4.dp

    private var viewWidth = 0
    private var viewHeight = 0

    private var currentValue = 0

    // 绘制表尺刻度的画笔
    private val linePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = scaleColor
            strokeWidth = scaleWidth.toFloat()
        }
    }

    // 绘制表尺中间标记的画笔
    private val pointPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeCap = Paint.Cap.ROUND
            strokeWidth = pointWidth.toFloat()
        }
    }

    init {
        scaleCount = scaleMaxValue - scaleMinValue
        currentValue = (scaleMaxValue - scaleMinValue) / 2
        Handler(Looper.getMainLooper()).postDelayed({
            setCurrentValue(0)
        }, 3000)
    }

    /*************************************测量******************************************************/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            measureHeight(heightMeasureSpec)
        )
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var height = 0
        when (heightMode) {
            // MeasureSpec.EXACTLY 为match_parent或者具体的值
            MeasureSpec.EXACTLY -> {
                height = heightSize
            }
            // MeasureSpec.AT_MOST 为wrap_content
            MeasureSpec.AT_MOST -> {
                // 高度 = 刻度尺长刻度的高度 + 上边距 + 下边距
                height = longScaleHeight.coerceAtLeast(pointHeight) + paddingTop + paddingBottom
                // 如果高度大于父容器给的高度,则取父容器给的高度
                height = height.coerceAtMost(heightSize)
            }
            // MeasureSpec.UNSPECIFIED 父容器对于子容器没有任何限制,子容器想要多大就多大,多出现于ScrollView
            MeasureSpec.UNSPECIFIED -> {
                height = heightSize
            }
        }
        return height
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        initParams()
    }

    private fun initParams() {
        // 长刻度的高度 = 控件高度 - 上边距 - 下边距
        longScaleHeight = height - paddingTop - paddingBottom
        // 短刻度的高度 = 长刻度的高度 - 15dp
        shortScaleHeight = longScaleHeight - 10.dp

    }

    /*************************************布局******************************************************/

    // 注意，由于会主动调用requestLayout(),所以不能复写kotlin的set方法。
    fun setCurrentValue(value: Int) {
        // 限制值的范围，避免越界
        var value = value
        if (value < scaleMinValue) {
            value = scaleMinValue
        }
        if (value > scaleMaxValue) {
            value = scaleMaxValue
        }
        currentValue = value
        // 更新当前选中值，并重新布局
        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 根据当前选中值计算滚动的距离 = (当前选中值 - 最小值) * 刻度间隔 - 控件宽度的一半
        val scrollX: Int = (currentValue - scaleMinValue) * scaleSpace - viewWidth / 2
        scrollTo(scrollX, 0)
    }

    /*************************************绘制******************************************************/

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (i in 0..scaleCount) {
            // 点从下往上绘制
            // 刻度起始的x坐标  = 刻度间隔 * i + 左边距
            val x1 = scaleSpace * i
            // 刻度起始的y坐标 = 上边距
            val y1 = height - paddingBottom
            // 刻度终点的x坐标 = 刻度起始的x坐标
            val x2 = x1
            // 刻度终点的y坐标 = 控件高度 - 下边距 - 刻度高度
            val y2 =
                height - paddingBottom - (if (i % 10 == 0) longScaleHeight else shortScaleHeight)

            canvas?.drawLine(
                x1.toFloat(),
                y1.toFloat(),
                x2.toFloat(),
                y2.toFloat(),
                linePaint
            )

        }
        drawCenterLine(canvas)
    }

    private fun drawCenterLine(canvas: Canvas?) {
        // 表尺中心点的x坐标 +滚动的距离是为了让中心点始终在屏幕中间
        val centerPointX = viewWidth / 2 + scrollX
        // 中间刻度的起始y坐标 = 上边距 - 5dp（加长5dp）
        val centerStartPointY = paddingTop - 5.dp
        // 中间刻度的终点y坐标 = 控件高度 - 下边距 + 5dp（加长5dp）
        val centerEndPointY = viewHeight - paddingBottom + 5.dp
        canvas?.drawLine(
            centerPointX.toFloat(), centerStartPointY.toFloat(),
            centerPointX.toFloat(), centerEndPointY.toFloat(),
            pointPaint
        )
    }

    /*************************************触摸******************************************************/

    private val scroller by lazy { OverScroller(context) }
    private val gestureDetector by lazy { GestureDetector(context, touchGestureListener) }

    private val touchGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 当监听到滑动事件时，滚动到指定位置
            scrollBy(distanceX.toInt(), 0)
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // 启动滚动器，设置滚动的起始位置，速度，范围和回弹距离
            scroller.fling(
                scrollX, 0,
                -velocityX.toInt() / 2, 0,
                -viewWidth / 2, (scaleCount - 1) * scaleSpace - viewWidth / 2,
                0, 0,
                viewWidth / 4, 0
            )
            invalidate()
            return true
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 当手指抬起时，校准位置
        if (event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            correctPosition()
        }
        return gestureDetector.onTouchEvent(event!!)
    }

    /*************************************惯性触摸******************************************************/

    private var currentX = 0;

    // 滚动方法
    override fun scrollTo(x: Int, y: Int) {
        Log.e("TAG", "scrollTo: ")
        // 限制滚动的范围，避免越界
        var x = x
        // 当x坐标小于可视区域的一半时，设置x坐标为可视区域的一半
        if (x < -viewWidth / 2) {
            x = -viewWidth / 2
        }
        // 当x坐标大于最大滚动距离时，设置x坐标为最大滚动距离
        if (x > (scaleCount) * scaleSpace - viewWidth / 2) {
            x = (scaleCount) * scaleSpace - viewWidth / 2
        }
        // 调用父类的滚动方法
        super.scrollTo(x, y)
        // 保存当前选中值
        currentValue = (x + viewWidth / 2) / scaleSpace + scaleMinValue
        currentX = x
        // 触发重绘，更新视图
        invalidate()
    }

    override fun computeScroll() {
        // 如果滚动器正在滚动，更新滚动的位置，并根据需要修正位置
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            if (scroller.isFinished) {
                correctPosition()
            }
            invalidate()
        }
    }

    // 修正位置,计算当前选中值距离最近的刻度的偏移量，并根据偏移量进行平滑滚动到正确的位置
    private fun correctPosition() {
        // 刻度值对应的x坐标
        val scaleX: Int = (currentValue - scaleMinValue) * scaleSpace - viewWidth / 2
        // 偏移值 = 刻度值对应的x坐标-当前x坐标 的绝对值
        val offset = (scaleX - currentX).absoluteValue
        if (offset == 0) {
            return
        }
        if (offset > scaleSpace / 2) {
            smoothScrollBy(scaleSpace - offset)
        } else {
            smoothScrollBy(-offset)
        }
    }

    // 平滑滚动方法
    private fun smoothScrollBy(dx: Int) {
        // 启动滚动器，设置滚动的起始位置，距离，时间和插值器
        scroller.startScroll(scrollX, 0, dx, 0, 200)
        invalidate()
    }
}