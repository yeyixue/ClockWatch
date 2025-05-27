package com.example.clockwatch
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.TimeZone

//表盘 画笔 时分秒指针

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //固定尺寸
    private val defaultSize = 360
    private var hour = 0
    private var minute = 0
    private var second = 0
    //typedArray可以获得设置的一系列属性
    var typedArray=context.obtainStyledAttributes(attrs,R.styleable.ClockView)
    var Clockcolor=typedArray.getColor(R.styleable.ClockView_clockColor,Color.BLACK)
    private val paint = Paint().apply {
        color = Clockcolor
        style = Paint.Style.STROKE  // 空心圆
        strokeWidth = 4f
    }
    private var timeZone: TimeZone = TimeZone.getDefault()

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    getTime()      // 更新时间
                    invalidate()      // 重绘界面
                    sendEmptyMessageDelayed(1, 1000) // 1秒后再次触发
                }
            }
        }
    }

    init {
        startClock()
    }

    // 新增时区设置方法
    fun setTimeZone(zone: TimeZone) {
        timeZone = zone
        getTime()
        invalidate()
    }
    private fun startClock() {
        handler.sendEmptyMessage(1)
    }
    //获取当前时间
    fun getTime(){
        //设置北京时间
//        val timeZone = TimeZone.getTimeZone(timeZone)
        val calendar = Calendar.getInstance(timeZone)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        second = calendar.get(Calendar.SECOND)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 1. 获取测量模式和尺寸
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // 2. 处理尺寸逻辑（确保宽高相同）
        val size = when {
            // 精确模式：取最小值保证圆形不变形
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY ->
                minOf(widthSize, heightSize)

            // 仅宽度为精确模式
            widthMode == MeasureSpec.EXACTLY -> widthSize

            // 仅高度为精确模式
            heightMode == MeasureSpec.EXACTLY -> heightSize

            // 其他情况（AT_MOST/UNSPECIFIED）：使用默认尺寸
            else -> defaultSize
        }
        // 3. 设置最终测量结果（强制宽高相同）
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//      getTime()
        setPadding(20,20,20,20)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f - 30

        //绘制内圆 圆1
        paint.style = Paint.Style.FILL
        paint.setColor(Color.argb(255,235, 235, 235))
        canvas.drawCircle(centerX, centerY, radius * 0.3f, paint)
        //绘制内圆 圆2
        paint.setColor(Color.argb(255,242, 242, 244))
        canvas.drawCircle(centerX, centerY, radius * 0.15f, paint)
        paint.style = Paint.Style.STROKE // 恢复描边模式
        paint.setColor(Color.argb(255,202, 202, 204))

        //表盘中央设置文字
        paint.textSize = radius * 0.20f  // 动态调整文字大小
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER  // 关键：文字居中
        // 格式化时间为"HH:mm"
        val timeText = String.format("%02d:%02d", hour, minute)
        // 计算文字基线位置（使文字垂直居中）
        val textY = centerY - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(timeText, centerX, textY, paint)
        paint.setColor(Color.argb(255,202, 202, 204))


        // 绘制12个刻度
        paint.strokeWidth = 8f
        val tickLength = radius * 0.05f
        for (i in 0 until 60) {
            canvas.save() // 保存当前画布状态
            canvas.rotate(6f * i, centerX, centerY) // 每次旋转30度
            if(i%5==0){
                if(i%15!=0){
                    // 绘制刻度线（从内圆边缘开始）
                    canvas.drawLine(
                        centerX, centerY - radius * 0.88f,
                        centerX, centerY - radius * 0.72f + tickLength,
                        paint
                    )
                }

            }else{
                canvas.drawLine(
                    centerX, centerY - radius * 0.75f,
                    centerX, centerY - radius * 0.85f + tickLength,
                    paint
                )
            }
            canvas.restore() // 恢复画布状态
        }
        //设置数字 12  3  6  9
        paint.strokeWidth = 12f
        paint.setColor(Color.argb(135,47, 47, 49))
        paint.style = Paint.Style.FILL
        paint.textSize = radius * 0.1f  // 数字大小
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("12",centerX,centerY - radius * 0.75f, paint)
        canvas.drawText("3",centerX + radius * 0.77f,centerY+radius * 0.05f , paint)
        canvas.drawText("6",centerX,centerY + radius * 0.79f, paint)
        canvas.drawText("9",centerX- radius * 0.77f,centerY+radius * 0.05f  , paint)
        paint.setColor(Color.argb(255,202, 202, 204))



        // 时针（每小时30度 + 每分钟0.5度）
        paint.strokeWidth = 30f
        paint.setColor(Color.argb(255,237, 76, 67))
        canvas.save()
        canvas.rotate(30f * (hour % 12) + 0.5f * minute, centerX, centerY)
        canvas.drawLine(
            centerX, centerY-radius * 0.3f,
            centerX, centerY - radius * 0.5f, // 时针长度
            paint
        )
        canvas.restore()

        // 分针（每分钟6度）
        paint.strokeWidth = 20f
        paint.setColor(Color.argb(255,46, 46, 48))
        canvas.save()
        canvas.rotate(6f * minute, centerX, centerY)
        canvas.drawLine(
            centerX, centerY-radius * 0.3f,
            centerX, centerY - radius * 0.65f, // 分针长度
            paint
        )
        canvas.restore()

        // 秒针（每秒6度）
        paint.strokeWidth = 10f
        paint.setColor(Color.argb(255,237, 76, 67))
        canvas.save()
        canvas.rotate(6f * second, centerX, centerY)
        canvas.drawLine(
            centerX, centerY - radius * 0.73f,
            centerX, centerY - radius * 0.89f + tickLength, // 秒针长度
            paint
        )
        canvas.restore()
    }

    //当包含这个视图的布局被添加到窗口时  开始每秒更新时间 ==>更平滑
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startClock()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeMessages(1)
    }
}