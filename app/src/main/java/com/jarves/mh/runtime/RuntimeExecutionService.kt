package com.jarves.mh.runtime

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager

internal object RuntimeTaskController {
    @Volatile var stopAction: (() -> Unit)? = null

    fun requestStop() {
        stopAction?.invoke()
    }
}

class RuntimeExecutionService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var projectName: String = "your project"
    private var notificationTitle: String = "Mobile Harness is working"
    private var canStop: Boolean = true
    private var taskRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        NotificationCoordinator.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_PROJECT_NAME)?.takeIf(String::isNotBlank)?.let { projectName = it }
        intent?.getStringExtra(EXTRA_TITLE)?.takeIf(String::isNotBlank)?.let { notificationTitle = it }
        if (intent?.hasExtra(EXTRA_CAN_STOP) == true) canStop = intent.getBooleanExtra(EXTRA_CAN_STOP, true)
        when (intent?.action ?: ACTION_START) {
            ACTION_STOP -> {
                RuntimeTaskController.requestStop()
                getSystemService(android.app.NotificationManager::class.java).notify(
                    NotificationCoordinator.RUNNING_NOTIFICATION_ID,
                    NotificationCoordinator.running(this, notificationTitle, "Stopping safely…", false),
                )
            }
            ACTION_PROGRESS -> {
                // Live step updates only matter while a task is actually running.
                if (!taskRunning) return START_NOT_STICKY
                val detail = intent?.getStringExtra(EXTRA_DETAIL)?.takeIf { it.isNotBlank() }
                    ?: "Claude Code is working in $projectName"
                getSystemService(android.app.NotificationManager::class.java).notify(
                    NotificationCoordinator.RUNNING_NOTIFICATION_ID,
                    NotificationCoordinator.running(this, notificationTitle, detail, canStop),
                )
            }
            ACTION_COMPLETE -> finishTask(
                title = "Task completed",
                detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "Mobile Harness finished working in $projectName.",
                failed = false,
            )
            ACTION_FAILED -> finishTask(
                title = "Task needs attention",
                detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "Mobile Harness could not finish the task.",
                failed = true,
            )
            ACTION_CANCELLED -> {
                taskRunning = false
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                taskRunning = true
                startForeground(
                    NotificationCoordinator.RUNNING_NOTIFICATION_ID,
                    NotificationCoordinator.running(
                        context = this,
                        title = notificationTitle,
                        detail = "Claude Code is working in $projectName",
                        canStop = canStop,
                    ),
                )
                acquireWakeLock()
            }
        }
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.jarves.mh:active-coding-task")
            .apply { acquire(MAX_WAKE_LOCK_MS) }
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.jarves.mh.START_RUNTIME"
        const val ACTION_STOP = "com.jarves.mh.STOP_RUNTIME"
        const val ACTION_PROGRESS = "com.jarves.mh.PROGRESS_RUNTIME"
        const val ACTION_COMPLETE = "com.jarves.mh.COMPLETE_RUNTIME"
        const val ACTION_FAILED = "com.jarves.mh.FAIL_RUNTIME"
        const val ACTION_CANCELLED = "com.jarves.mh.CANCEL_RUNTIME"
        const val EXTRA_PROJECT_NAME = "project_name"
        const val EXTRA_DETAIL = "detail"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CAN_STOP = "can_stop"

        private const val MAX_WAKE_LOCK_MS = 90 * 60 * 1_000L
    }

            )
            manager.createNotificationChannel(
                NotificationChannel(RESULT_CHANNEL_ID, "Task results", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Notifies you when a coding task finishes or needs attention"
                },
            )
        }
    }
}
