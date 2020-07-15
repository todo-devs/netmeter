package com.cubanopensource.netmeter

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {
    private val CHANNEL = "todonetmeter_android"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->

            when {
                call.method == "getPlatformVersion" -> result.success("Android ${Build.VERSION.RELEASE}")
                call.method == "getDrawPermissionState" -> result.success(getDrawPermissionState())
                call.method == "reqDrawPermission" -> result.success(reqDrawPermission())
                call.method == "showWidget" -> showFloatWidget()
                else -> result.notImplemented()
            }

        }
    }

    private fun getDrawPermissionState(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return (Settings.canDrawOverlays(this))

        return true
    }

    private fun reqDrawPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
            startActivityForResult(intent, 1)
        }
    }

    private fun showFloatWidget() {
        if (getDrawPermissionState()) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
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
            val widgetView = LayoutInflater.from(this).inflate(R.layout.float_window, null)

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
                                println("POR FIN")
                                wm.removeView(widgetView)
                            }

                            closeWM.removeView(closeView)
                        }
                    }

                    return false
                }
            })


        }
    }
}

