package com.systemnoxltd.hotelmatenox.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.systemnoxltd.hotelmatenox.utils.NotificationHelper
import com.systemnoxltd.hotelmatenox.utils.hasPostNotificationsPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReportNotification(val uri: Uri)

class NotificationViewModel : ViewModel() {

    private val _pending = MutableStateFlow<ReportNotification?>(null)
    val pending = _pending.asStateFlow()

    fun queue(notification: ReportNotification) {
        _pending.value = notification
    }

    /**
     * Try to post any queued notification; return true if posted.
     */
    fun tryPostPendingIfPermitted(context: Context): Boolean {
        val n = _pending.value ?: return false
        if (hasPostNotificationsPermission(context)) {
            NotificationHelper.showReportNotification(context, n.uri)
            _pending.value = null
            return true
        }
        return false
    }

    fun clear() { _pending.value = null }
}
