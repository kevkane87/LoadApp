package com.udacity

/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0

@SuppressLint("UnspecifiedImmutableFlag", "WrongConstant")
fun NotificationManager.sendNotification(fileName: String, isSuccess: Boolean, uriString: String, applicationContext: Context) {

    //Intent to return to main activity from notification
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //Intent to go to detail activity and pass arguments from main activity
    val detailIntent = Intent(applicationContext, DetailActivity::class.java)
    detailIntent.putExtra("status", isSuccess)
    detailIntent.putExtra("file", fileName)
    detailIntent.putExtra("uri", uriString)
    val detailPendingIntent: PendingIntent = PendingIntent.getActivity(
        applicationContext,
        REQUEST_CODE,
        detailIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    val statusImage: Bitmap
    val messageBody: String

    //determines if download is success or fail and sets variable attributes for the notification
    if (isSuccess){
        messageBody = applicationContext.getString(R.string.download_successful)
        statusImage = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.download_complete)
    }
    else {
        messageBody = applicationContext.getString(R.string.download_failed)
        statusImage = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.download_failed
        )
    }

    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(statusImage)
        .bigLargeIcon(null)

    //Notification builder to implement notification with attributes
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(bigPicStyle)
        .setLargeIcon(statusImage)
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            detailPendingIntent
        )

    notify(NOTIFICATION_ID, builder.build())
}

