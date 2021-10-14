package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Attendees.query
import android.util.Patterns
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.reflect.Array.getInt
import androidx.core.content.FileProvider
import java.io.File
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var radioGroup: RadioGroup
    private lateinit var editTextCustom: EditText
    private lateinit var fileName: String

    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //register receiver for when a download is complete
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        radioGroup = findViewById(R.id.radio_group)
        editTextCustom = findViewById(R.id.editTextCustom)

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        //creates notification channel
        createChannel(getString(R.string.download_notification_channel_id), getString(R.string.download_notification_channel_name))

        //on button click the correct file to download will be assigned based on user selected radio button
        custom_button.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                -1 -> {
                    Toast.makeText(this, getString(R.string.select_file), Toast.LENGTH_SHORT)
                        .show()
                    custom_button.animationComplete()
                }
                R.id.radioButtonGlide -> {
                    url = URL_GLIDE
                    fileName = getString(R.string.glide_file)
                }
                R.id.radioButtonLoadApp -> {
                    url = URL_LOAD_APP
                    fileName = getString(R.string.load_app_file)
                }
                R.id.radioButtonRetrofit -> {
                    url = URL_RETROFIT
                    fileName = getString(R.string.retrofit_file)
                }
                R.id.radioButtonCustom -> {
                    //if the file is a custom url, check if it's a valid url
                    if (Patterns.WEB_URL.matcher(editTextCustom.text.toString()).matches() && URLUtil.isValidUrl(editTextCustom.text.toString())){
                        url = editTextCustom.text.toString()
                        fileName = getString(R.string.custom_file)
                    }

                    //if user has not selected anything a toast will be displayed
                    else{
                        url = ""
                        Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT)
                            .show()
                        custom_button.animationComplete()
                    }
                }
            }
            //start download
            if (URLUtil.isValidUrl(url))
                download()
        }
    }

    //Broadcast receiver triggered when download manager detects a complete download
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            //signal animation to stop
            custom_button.animationComplete()

            //gets completed download ID
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            //check for matching download ID
            if (id == downloadID) {

                //query status of download
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    )
                    when (status) {
                        //when successful set the notification for successful and pass arguments for details activity
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uriString: String = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            notificationManager.sendNotification(fileName,true, uriString,context!!)
                        }
                        //when failed set the notification for failed and pass arguments for details activity
                        DownloadManager.STATUS_FAILED -> {
                            notificationManager.sendNotification(fileName,false,"",context!!)
                        }
                        //if status is paused or pending, notify by toast
                        DownloadManager.STATUS_PAUSED-> {
                            Toast.makeText(context, getString(R.string.paused), Toast.LENGTH_SHORT)
                                .show()
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Toast.makeText(context, getString(R.string.pending), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }


    //function to set parameters and start download using DownloadManager
    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }


    //Receiver unregistered on destroy
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    //function to create notification channel using NotificationManager
    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.download_status)

            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    companion object {
        private const val URL_LOAD_APP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE =
        "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"

    }
}
