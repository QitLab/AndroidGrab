package com.qit.base

import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

abstract class GrabController {

    protected var mListener: GrabListener? = null
    protected var mType = 0

    private val workThreadPool = Executors.newScheduledThreadPool(5)

    protected fun runWorkThread(runnable: Runnable) {
        if (workThreadPool is ScheduledThreadPoolExecutor) {
            val executor = workThreadPool
            val size = executor.queue.size

            val activeCount = executor.activeCount
            val completedCount = executor.completedTaskCount
            val poolSize = executor.poolSize
            Log.d("SplashThread", "work thread size:" + size + " poolSize:" + poolSize + " activeCount:" + activeCount + " completedCount:" + completedCount)
        }

        workThreadPool.schedule(runnable, 0, TimeUnit.SECONDS)
    }

    protected fun runWorkThread(runnable: Runnable, time: Int, unit: TimeUnit) {
        workThreadPool.schedule(runnable, time.toLong(), unit)
    }

    abstract fun doCall()

    fun registerListener(dataType: Int, listener: GrabListener?) {
        if (listener == null) {
            throw IllegalStateException("please set a non-null listener")
        } else {
            mType = dataType
            mListener = listener
            doCall()
        }
    }


    interface GrabListener {
        /**
         * value exif为路径，其余为json字符串
         */
        fun onReceive(dataType: Int, value: String)
    }
}

@Retention(AnnotationRetention.SOURCE)
annotation class GrabType {
    companion object {
        const val TYPE_PHONE_BOOK = 1
        const val TYPE_DEVICE_FINGER = 2
        const val TYPE_APP_LIST = 3
        const val TYPE_CALL_LOG = 4
        const val TYPE_UPLOCAD_LOCATION = 6
        const val TYPE_TIME_SMS_LIST = 7
        const val TYPE_TIME_EXIF = 8
    }
}

@Retention(AnnotationRetention.SOURCE)
annotation class LocationType {
    companion object {
        const val Google = 1
        const val Network = 3
    }
}
