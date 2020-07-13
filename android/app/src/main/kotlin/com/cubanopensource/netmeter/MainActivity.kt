package com.cubanopensource.netmeter

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "todonetmeter_android"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->

            when {
                call.method == "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
                else -> result.notImplemented()
            }

        }
    }
}
