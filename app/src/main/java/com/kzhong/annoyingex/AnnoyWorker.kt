package com.kzhong.annoyingex

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlin.random.Random

class AnnoyWorker(private val context: Context, workParams: WorkerParameters): Worker(context , workParams) {
    private val notificationManagerCompat = NotificationManagerCompat.from(context)
    private val queue: RequestQueue = Volley.newRequestQueue(context)
    private var messages:Messages? = null

    init {
        createFunChannel()
    }

    override fun doWork(): Result {
        fetch({response->
            messages = response
            pushNotification()
        },{pushNotification()})


        return Result.success()
    }

    private fun pushNotification() {
        var message = "unable to retrieve message"

        if(messages != null) {
            message = messages!!.messages[Random.nextInt(0, messages!!.messages.size)]
        }
        val intent = Intent(context, NewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("hello", message)
        }

        val pendingDealsIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val notification = NotificationCompat.Builder(context, FUN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Order delivered")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingDealsIntent)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.notify(Random.nextInt(), notification)
    }

    private fun fetch(onQuoteReady: (Messages) -> Unit, onError: (() -> Unit)? = null) {
        val url = "https://raw.githubusercontent.com/echeeUW/codesnippets/master/ex_messages.json"
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Success
                val gson = Gson()
                val messages = gson.fromJson(response, Messages::class.java)
                onQuoteReady(messages)

            },
            {
                onError?.invoke()
            }
        )

        queue.add(request)
    }

    private fun createFunChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fun Notifications"
            val descriptionText = "All Msgs from a great autotune voiced dude"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(FUN_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManagerCompat.createNotificationChannel(channel)
        }
    }

    companion object {
        const val FUN_CHANNEL_ID = "FUNCHANNELID"
    }
}