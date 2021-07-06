package com.team15.skredet.externalFiles

/**
 * Problem: The intent created by the notification is not able to boot the application from another place than MainActivity
 *      When pushed, the notification creates a new instance of the application, and does not know where to go
 * Solution: When a new intent is created for the notification, a unique flag-number is set
 *      This number can be compared to the intent.flag that launched the application
 *
 */
data class NotificationIntent(
    val name: String, //setting or favorite list
    val flag: Int
)