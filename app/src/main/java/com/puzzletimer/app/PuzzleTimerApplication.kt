package com.puzzletimer.app

import android.app.Application
import com.puzzletimer.app.di.AppContainer

class PuzzleTimerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
