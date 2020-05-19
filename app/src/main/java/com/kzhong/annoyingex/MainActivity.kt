package com.kzhong.annoyingex

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.work.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var workManager = WorkManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnHereWeGo.setOnClickListener {
            startAnnoy()
        }

        btnBlock.setOnClickListener {
            stopWork()
        }
    }

    private fun startAnnoy() {
        if (isRunning()) {
            stopWork()
        }

        val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .build()

        val workRequest = PeriodicWorkRequestBuilder<AnnoyWorker>(1000, TimeUnit.MILLISECONDS)
                .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(WORK_REQUEST_TAG)
                .build()


        workManager.enqueue(workRequest)

    }



    private fun isRunning(): Boolean {
        return when (workManager.getWorkInfosByTag(WORK_REQUEST_TAG).get().firstOrNull()?.state) {
            WorkInfo.State.RUNNING,
            WorkInfo.State.ENQUEUED -> true
            else -> false
        }
    }

    private fun stopWork() {
        workManager.cancelAllWorkByTag(WORK_REQUEST_TAG)
    }

    companion object{
        const val WORK_REQUEST_TAG = "workRequestTag"
    }
}
