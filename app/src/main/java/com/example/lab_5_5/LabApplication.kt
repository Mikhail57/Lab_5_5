package com.example.lab_5_5

import android.app.Application
import com.example.lab_5_5.camera.cameraModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LabApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LabApplication)

            modules(cameraModule)
        }
    }
}