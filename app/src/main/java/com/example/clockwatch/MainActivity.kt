package com.example.clockwatch
import com.example.clockwatch.Util.TimeZoneUtils

import android.app.AlertDialog
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var clockview:ClockView
    private lateinit var tvMDW: TextView
    private lateinit var choseTime: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var digitalView: TextView? = null
    private var updateRunnable: Runnable? = null
    private lateinit var btnzone:ImageButton
    private var timezoneName: String="Asia/Shanghai"
    private val countryTimeZones = TimeZoneUtils.countryTimeZones


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clock_view)

        //绑定控件
        bindViews()
        // 初始更新时间
        updateDate(tvMDW)
        // 设置每天0点自动更新日期
        tvMDW.postDelayed(object : Runnable {
            override fun run() {
                updateDate(tvMDW)
                tvMDW.postDelayed(this, getMillisToNextDay())
            }
        }, getMillisToNextDay())

        clockview.setOnClickListener {
            showDigitalClock()
        }
        btnzone.setOnClickListener {
            showTimezonePicker()
        }
        clockview.setTimeZone(TimeZone.getTimeZone(timezoneName))
    }

    //绑定控件id
    private fun bindViews() {
        tvMDW = findViewById(R.id.tvMDW)
        choseTime = findViewById(R.id.choseTime)
        clockview = findViewById(R.id.clockView)
        btnzone = findViewById(R.id.btnZone)
    }

    //实现点击切换视图
    private fun showDigitalClock() {
        Log.d("clockview.width",clockview.width.toString())   //945
        Log.d("clockview.height",clockview.height.toString()) //945
        //创建新TextView
        digitalView = TextView(this).apply {
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            // 设置点击返回功能
            setOnClickListener {
                returnToClockView()
            }
        }

        // 替换ClockView（保留其他视图）
        val parent = clockview.parent as ViewGroup
        val index = parent.indexOfChild(clockview)
        parent.removeView(clockview)
        parent.addView(digitalView, index)

        updateDigitalClockTimeZone()
    }


    private fun returnToClockView() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        setContentView(R.layout.activity_clock_view)

        bindViews()
        updateDate(tvMDW)
        scheduleDailyUpdate()
        //关键代码：恢复当前时区显示文字
        val currentCountry = countryTimeZones.entries.find { it.value == timezoneName }?.key ?: "中国"
        choseTime.text = "$currentCountry"+"标准时间"
        clockview.setOnClickListener {
            showDigitalClock()
        }

        btnzone = findViewById(R.id.btnZone)
        btnzone.setOnClickListener {
            showTimezonePicker()
        }

        clockview.setTimeZone(TimeZone.getTimeZone(timezoneName))
    }

    //通过Android的 Handler 机制，在指定的延迟时间后执行 Runnable。
    private fun scheduleDailyUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateDate(tvMDW)
                handler.postDelayed(this, getMillisToNextDay())
            }
        }, getMillisToNextDay())
    }

    //选择时区国家
    private fun showTimezonePicker() {
        var selectcountries = countryTimeZones.keys.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("选择国家/地区")
        builder.setItems(selectcountries) { _, which ->
            val selectedCountry = selectcountries[which]
            timezoneName = countryTimeZones[selectedCountry] ?: "Asia/Shanghai"

            clockview.setTimeZone(TimeZone.getTimeZone(timezoneName))
            updateDigitalClockTimeZone()
            val str = "$selectedCountry"+"标准时间"
            choseTime.text = str
        }

        builder.show()
    }


    private fun updateDigitalClockTimeZone() {
        updateRunnable?.let { handler.removeCallbacks(it) }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone(timezoneName)
        }
        updateRunnable = object : Runnable {
            override fun run() {
                digitalView?.text = timeFormat.format(Date())
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }


    //更新文本
    private fun updateDate(textView: TextView) {
        val timezone = TimeZone.getTimeZone(timezoneName)
        val calendar = Calendar.getInstance(timezone)
        // 设置中文日期格式（5月26日 星期一）
        val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINA).apply {
            timeZone = timezone
        }

        textView.text = dateFormat.format(calendar.time)
    }

    private fun getMillisToNextDay(): Long {
        val timezone = TimeZone.getTimeZone(timezoneName)
        val calendar = Calendar.getInstance(timezone)

        // 计算到第二天0点的毫秒数
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        return (24 * 3600 - currentHour * 3600 - currentMinute * 60 - currentSecond) * 1000L
    }



}




