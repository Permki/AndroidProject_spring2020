package com.team15.skredet.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.team15.skredet.R

//Reference to online guide: https://www.raywenderlich.com/1214490-android-notifications-tutorial-getting-started#toc-anchor-016
object NotificationHelper {
    var notificationID = 1

    /**
     * Sets up the notification channels for API 26+.
     * Note: This uses package name + channel name to create unique channelId's.
     * @author Haakose
     * @param context     application context
     * @param importance  importance level for the notificaiton channel
     * @param showBadge   whether the channel should have a notification badge
     * @param name        name for the notification channel
     * @param description description for the notification channel
     */
    fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            // Register the channel with the system
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Helps issue the default application channels (package name + app name) notifications.
     * Note: this shows the use of [NotificationCompat.BigTextStyle] for expanded notifications.
     * @author Haakose
     * @param context    current application context
     * @param title      title for the notification
     * @param message    content text for the notification when it's not expanded
     * @param bigText    long form text for the expanded notification
     * @param autoCancel `true` or `false` for auto cancelling a notification.
     * if this is true, a [PendingIntent] is attached to the notification to
     * open the application.
     * @param picture    reference to the placement of the picture Icon: example(R.drawable.ic_warning_black_24dp)
     */
    fun createDataNotification(
        context: Context, title: String, message: String,
        bigText: String, autoCancel: Boolean, picture: Int,
        openPage: Class<*>
    ) {

        //Create unique channelId for this app using the package name and app name
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        //Use NotificatinCompat.Buider to begin building the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(picture)
            setContentTitle(title)
            setContentText(message)
            setAutoCancel(autoCancel)
            setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            //set priority, indicates how much of the user¨s attention the notification should draw
            priority = NotificationCompat.PRIORITY_DEFAULT
            //When tapped: Auto cancel
            setAutoCancel(autoCancel)

            //Direct the user when tapped, Intent to launch MainActivity
            val intent = Intent(context, openPage)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            //pendingIntent = action taken at a later point, wraps intent
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            //Attach to notificationCompat.Builder
            setContentIntent(pendingIntent)
        }

        //Notify the manager, use app's context to get a reference to NotificationManagerCompat
        val notificationManager = NotificationManagerCompat.from(context)
        //Call notify() on NotificationManager passing an identifier and the notification
        notificationID++
        notificationManager.notify(notificationID, notificationBuilder.build())
    }
}