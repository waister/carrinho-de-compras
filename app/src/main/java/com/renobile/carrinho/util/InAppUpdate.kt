package com.renobile.carrinho.util

import android.content.IntentSender
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.renobile.carrinho.R

class InAppUpdate(private val activity: AppCompatActivity) : InstallStateUpdatedListener, LifecycleEventObserver {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val appUpdateInfo = appUpdateManager.appUpdateInfo
    private var currentType = AppUpdateType.FLEXIBLE

    init {
        appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                startUpdate(info, AppUpdateType.FLEXIBLE)
            }
        }

        appUpdateManager.registerListener(this)
        activity.lifecycle.addObserver(this)
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            flexibleUpdateDownloadCompleted()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
        Lifecycle.Event.ON_RESUME -> onResume()
        Lifecycle.Event.ON_DESTROY -> appUpdateManager.unregisterListener(this)
        else -> Unit
    }

    private fun onResume() {
        appUpdateInfo.addOnSuccessListener { info ->
            if (currentType == AppUpdateType.FLEXIBLE) {
                // If the update is downloaded but not installed, notify the user to complete the update.
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    flexibleUpdateDownloadCompleted()
                }
            } else if (currentType == AppUpdateType.IMMEDIATE) {
                // for AppUpdateType.IMMEDIATE only, already executing updater
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startUpdate(info, AppUpdateType.IMMEDIATE)
                }
            }
        }
    }

    private fun startUpdate(info: AppUpdateInfo, type: Int) {
        if (
            activity.isFinishing ||
            activity.isDestroyed ||
            !activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        ) {
            return
        }
        try {
            @Suppress("DEPRECATION")
            appUpdateManager.startUpdateFlowForResult(
                info,
                activity,
                AppUpdateOptions.newBuilder(type).build(),
                500,
            )
            currentType = type
        } catch (e: IntentSender.SendIntentException) {
            appLog("IN_APP_UPDATE", "Failed to start update flow: ${e.message}")
        } catch (e: Throwable) {
            if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun flexibleUpdateDownloadCompleted() {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.update_alert_title)
            .setMessage(R.string.update_alert_message)
            .setCancelable(false)
            .setPositiveButton(R.string.update_alert_button) { dialog, _ ->
                appUpdateManager.completeUpdate()
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
