package com.team15.skredet.notif

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import com.team15.skredet.MainActivity
import com.team15.skredet.R
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import com.team15.skredet.getDate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 * This class configures what the background process will be doing
 * Please be noted that a Worker must be valued as highly unprecise in it's
 * stackpushes, so functionality should be nested
 * @author Haakose
 * The parameters are not configured by us, to send information to a worker, please use
 * createInputDataForWorker() in the MainActivity.kt
 */
class BackgroundWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    var cont = appContext

    //Fetch the avalancheValueList the user wants to get notifications about
    var arrInt = inputData.getIntArray("1")
    var arrStr = inputData.getStringArray("2")

    //Fetch what type of notification the user wants
    //We always assume that at least one of the numbers are 1, or else this worker is not called
    var notifInt = inputData.getIntArray("3")

    var internetNotified = false
    var mainInfoMap = hashMapOf<Int, AllRegionsDetailsData>()

    //Defines the unit of work
    override fun doWork(): Result {

        //Ha en skru av bakgrunnssjekking i instillinger
        //Ta inn instillingspefikasjoner: Bare increase, bare decrease, ingen (håndteres i main),
        fetchAPI()

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun fetchAPI() = MainScope().launch {

        try {
            val response = Fuel.get(
                cont.getString(R.string.region_summary_simple).format(getDate(0), getDate(0))
            ).awaitString()
            for (element in Gson().fromJson(response, Array<AllRegionsDetailsData>::class.java)
                .toMutableList())
                mainInfoMap[element.Id] = element
        } catch (e: Exception) {
            failedToUpdate()
            Result.failure()
        }

        //BackgroundWorker is not precise in its stackcalls, so the functionality must be nested like this
        if (!arrStr.isNullOrEmpty()) checkForChange()
        else {
            println("Backgroundworker sin arrStr er null")
        }
    }

    private fun checkForChange() {
        var counter = 0
        for (key in arrInt!!) {
            if (mainInfoMap.containsKey(key)) {
                val old = arrStr?.get(counter)?.toInt()
                val new = mainInfoMap[key]?.AvalancheWarningList?.get(0)?.DangerLevel?.toInt()

                //New is higher, avalanche have increased, send notification and update checkvalue
                if (new != null) {
                    if ((new > old!!) && notifInt!![0] == 1) {
                        avalancheIncreased(mainInfoMap[key]?.Name)
                    }
                    //New is lower, avalanche have decreased, send notif and update checkvalue
                    if ((new < old) && notifInt!![1] == 1) {
                        avalancheDecreased(mainInfoMap[key]?.Name)
                    }
                    arrStr?.set(
                        counter,
                        mainInfoMap[key]?.AvalancheWarningList?.get(0)?.DangerLevel
                    )
                }
                break
            }
            counter += 1
        }
    }

    private fun failedToUpdate() {
        if (!internetNotified) {
            genericNotification(
                "FailedToUpdate",
                "show notification that tells it failed to update",
                "Sjekk av snøskredfare mislyktes",
                "Vennligst sjekk din nettverkstilkobling",
                R.drawable.icons8_without_internet_50
            )
            internetNotified = true
        }
    }

    private fun avalancheIncreased(regionName: String?) {
        val message =
            if (regionName == "") "Snøskredfaren i ett eller flere av dine områder har økt, trykk her for å få mer informasjon"
            else "Snøskredfaren i $regionName har økt, trykk her for å få mer informasjon"

        genericNotification(
            "IncreaseNotification",
            "Showcast a notification that tells the user the danger is increased",
            "Snøskredfare har økt!",
            message,
            R.drawable.ic_warning_black_24dp
        )
    }

    private fun avalancheDecreased(regionName: String?) {
        val message =
            if (regionName == "") "Snøskredfaren i ett eller flere av dine områder har minsket, trykk her for å få mer informasjon"
            else "Snøskredfaren i $regionName har minsket, trykk her for å få mer informasjon"

        genericNotification(
            "DecreaseNotification",
            "Showcast a notification that tells the user the danger is decreased",
            "Snøskredfare har minsket!",
            message,
            R.drawable.ic_warning_black_24dp
        )
    }

    /**
     * Creates a standard notification when called, should be used as template
     * @author Haakose
     * @param name      Name of the notification (not visible)
     * @param description Descibres the notification (not visible)
     * @param title     First sentence of a notification (visible)
     * @param message   The rest of the notification (visible)
     * @param icon      reference to the icon (visible)
     *              A nice library for icons: https://icons8.com/icons/
     */
    private fun genericNotification(
        name: String,
        description: String,
        title: String,
        message: String,
        icon: Int
    ) {
        NotificationHelper.createNotificationChannel(
            applicationContext,
            NotificationManagerCompat.IMPORTANCE_HIGH, false,
            name, description
        )

        NotificationHelper.createDataNotification(
            applicationContext,
            title,
            message,
            message,
            true,
            icon,
            MainActivity::class.java
        )
    }
}
