package edu.farmingdale.threadsexample.countdowntimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import edu.farmingdale.threadsexample.R
import kotlinx.coroutines.delay
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import kotlin.time.Duration.Companion.milliseconds

const val CHANNEL_ID_TIMER = "channel_timer"
const val NOTIFICATION_ID = 0
const val KEY_MILLIS_REMAINING = 2000

class TimerWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        var remainingMillis = inputData.getLong(KEY_MILLIS_REMAINING.toString(), 0)

        if (remainingMillis == 0L) {
            return Result.failure()
        }

        createTimerNotificationChannel()

        while (remainingMillis > 0) {
            postTimerNotification(timerText(remainingMillis))
            delay(1000)
            remainingMillis -= 1000
        }

        // Post final notification
        postTimerNotification("Timer is finished!")




        return Result.success()
    }

    private fun createTimerNotificationChannel() {
        // Notification channels only available on O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID_TIMER, "Timer Channel",
                NotificationManager.IMPORTANCE_LOW)
            channel.description = "Displays how much time is left"

            // Register channel
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun postTimerNotification(text: String) {
        // Create notification with various properties
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID_TIMER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Post notification
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        Log.d("TimerWorker", text)
    }

    private fun timerText(timeInMillis: Long): String {
        val duration = timeInMillis.milliseconds
        return String.format("%02d:%02d:%02d",
            duration.inWholeHours, duration.inWholeMinutes % 60, duration.inWholeSeconds % 60)
    }

}