package com.example.j_scenario

import android.app.Application
import timber.log.Timber

class JScenarioApplication : Application() {
    
    companion object {
        @Volatile
        private var instance: JScenarioApplication? = null
        
        fun getInstance(): JScenarioApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // 프로덕션에서는 릴리즈 트리 사용 (필요시 Crashlytics 등 연동)
            Timber.plant(Timber.DebugTree()) // 임시로 DebugTree 사용
        }
        
        Timber.d("JScenarioApplication onCreate")
    }
}

