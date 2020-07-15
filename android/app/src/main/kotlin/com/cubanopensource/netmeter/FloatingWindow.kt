package com.cubanopensource.netmeter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView


class FloatingWindow : Service() {

    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0

    private val mHandler = Handler()

    private lateinit var tv: TextView

    private val mHandlerRunnable = object : Runnable {
        override fun run() {
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            val usedRxBytes = currentRxBytes - mLastRxBytes
            val usedTxBytes = currentTxBytes - mLastTxBytes
            val currentTime = System.currentTimeMillis()
            val usedTime = currentTime - mLastTime

            mLastRxBytes = currentRxBytes
            mLastTxBytes = currentTxBytes
            mLastTime = currentTime

            tv.text = calcSpeed(usedTime, usedRxBytes, usedTxBytes)

            mHandler.postDelayed(this, 1000)
        }
    }

    fun calcSpeed(timeTaken: Long, downBytes: Long, upBytes: Long): String {
        var downSpeed: Long = 0
        var upSpeed: Long = 0

        if (timeTaken > 0) {
            downSpeed = downBytes * 1000 / timeTaken
            upSpeed = upBytes * 1000 / timeTaken
        }

        val mDownSpeed = downSpeed
        val mUpSpeed = upSpeed

        val down = setSpeed(mDownSpeed)
        val up = setSpeed(mUpSpeed)

        return "↑$up ↓$down "
    }

    private fun setSpeed(speed: Long): String {
        if (speed < 1000000) {
            return "%.1f KB".format((speed / 1000.0))
        } else if (speed >= 1000000) {
            if (speed < 10000000) {
                return "%.1f MB".format((speed / 1000000.0))
            } else if (speed < 100000000) {
                return "%.1f MB".format((speed / 1000000.0))
            } else {
                return "+99 MB"
            }
        } else {
            return "-"
        }
    }

    override fun onCreate() {
        super.onCreate()

        mLastRxBytes = TrafficStats.getTotalRxBytes()
        mLastTxBytes = TrafficStats.getTotalTxBytes()
        mLastTime = System.currentTimeMillis()

        showFloatWidget()
    }

    private fun showFloatWidget() {
        if (getDrawPermissionState()) {
            val displayMetrics = DisplayMetrics()
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels

            // Close traffic stats widget
            val closeView = LayoutInflater.from(this).inflate(R.layout.close_float_widget, null)

            val closeWM = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val parameters_close: WindowManager.LayoutParams

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                parameters_close = WindowManager.LayoutParams(
                        width,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                )
            else
                parameters_close = WindowManager.LayoutParams(
                        width,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                )

            parameters_close.gravity = Gravity.BOTTOM

            // Traffic Stats widget
            val widgetView = LayoutInflater.from(this).inflate(R.layout.float_window, null) as RelativeLayout

            /*
            <TextView
                android:padding="2dp"
                android:id="@+id/traffic_text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_centerInParent="true"
                android:text="up 0 | down 0"
                android:textColor="@android:color/white"
                android:textStyle="bold" />
             */
            tv = TextView(this)
            tv.setPadding(2, 4, 2, 4)
            tv.setTextColor(Color.WHITE)
            tv.textSize = 11f
            tv.text = "up 0 | down 0"

            widgetView.addView(tv)

            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val parameters: WindowManager.LayoutParams

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                parameters = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                )
            else
                parameters = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                )

            parameters.x = 0
            parameters.y = 100
            parameters.gravity = Gravity.CENTER
            wm.addView(widgetView, parameters)

            // Traffic stats on touch handler
            widgetView.setOnTouchListener(object : View.OnTouchListener {
                private var updatedParameters: WindowManager.LayoutParams = parameters
                var x: Int = 0
                var y: Int = 0

                var touchedX: Float = 0.0f
                var touchedY: Float = 0.0f

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {

                            x = updatedParameters.x
                            y = updatedParameters.y

                            touchedX = event.rawX
                            touchedY = event.rawY

                            closeWM.addView(closeView, parameters_close)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            updatedParameters.x = (x + (event.rawX - touchedX)).toInt()
                            updatedParameters.y = (y + (event.rawY - touchedY)).toInt()

                            wm.updateViewLayout(widgetView, updatedParameters)
                        }

                        MotionEvent.ACTION_UP -> {
                            if (event.rawY.toInt() >= height - 100) {
                                wm.removeView(widgetView)
                            }

                            closeWM.removeView(closeView)
                        }
                    }

                    return false
                }
            })
        }

        mHandler.post(mHandlerRunnable)
    }

    private fun getDrawPermissionState(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return (Settings.canDrawOverlays(this))

        return true
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}