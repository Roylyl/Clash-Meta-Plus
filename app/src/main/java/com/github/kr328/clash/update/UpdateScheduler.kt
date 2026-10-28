package com.github.kr328.clash.update

import android.content.Context
import android.os.Build
import androidx.work.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object UpdateScheduler {
    private val network = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(3, TimeUnit.HOURS)
            .setInitialDelay(3, TimeUnit.HOURS)
            .setConstraints(network).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "app-release-periodic", ExistingPeriodicWorkPolicy.KEEP, request,
        )
    }

    fun checkOnLaunch(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "app-release-launch", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<UpdateCheckWorker>().setConstraints(network).build(),
        )
    }
}

class UpdateCheckWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    override fun doWork(): Result {
        return try {
            val current = com.github.kr328.clash.BuildConfig.VERSION_NAME
            val release = ReleaseRepository.fetchLatest(current, Build.SUPPORTED_ABIS.toList())
            if (release != null) {
                val preferences = applicationContext.getSharedPreferences("app_release_checks", Context.MODE_PRIVATE)
                if (preferences.getString("notified_tag", null) != release.tagName &&
                    UpdateNotifications.available(applicationContext, release.versionName)) {
                    preferences.edit().putString("notified_tag", release.tagName).apply()
                }
            }
            Result.success()
        } catch (_: IOException) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}

