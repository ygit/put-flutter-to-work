package com.example.newsfeed_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newsfeed_android.databinding.ActivityMainBinding
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

private const val FLUTTER_ENGINE_NAME = "nps_flutter_engine_name"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        warmupFlutterEngine()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        runFlutterNPS()
    }

    private fun runFlutterNPS() {
        startActivity(
            TestFlutterActivity.withCachedEngine(FLUTTER_ENGINE_NAME)
                .backgroundMode(FlutterActivityLaunchConfigsTest.BackgroundMode.transparent)
                .build(this)
        )
        finish()
    }

    private fun warmupFlutterEngine() {
        val flutterEngine = FlutterEngine(this)

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
            .getInstance()
            .put(FLUTTER_ENGINE_NAME, flutterEngine)
    }

}
