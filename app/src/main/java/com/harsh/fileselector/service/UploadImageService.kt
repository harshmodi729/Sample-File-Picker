package com.harsh.fileselector.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.harsh.fileselector.R
import com.harsh.fileselector.base.ProgressRequestBody
import com.harsh.fileselector.data.ApiInterface
import com.harsh.fileselector.data.AppDatabase
import com.harsh.fileselector.data.RetrofitClient
import com.harsh.fileselector.model.ImageItem
import com.harsh.fileselector.model.UploadImage
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class UploadImageService : IntentService("upload_image"), ProgressRequestBody.FileUploaderCallback {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: Notification.Builder
    private var items = ArrayList<ImageItem>()

    companion object {
        private const val CHANNEL_ID = "upload_image_notification"
        private const val CHANNEL_NAME = "UPLOAD_IMAGE_NOTIFICATION"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification()
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            intent?.let {
                items = it.getSerializableExtra("files") as ArrayList<ImageItem>
            }
            var totalFileLength = 0L
            items.forEach {
                totalFileLength += it.imageFile!!.length()
            }
            uploadImageIndex = 0
            if (items.isNotEmpty()) {
                uploadFile(items[uploadImageIndex])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uploadFile(item: ImageItem) {
        val fileBody = ProgressRequestBody(item.imageFile!!, this)
        val filePart =
            MultipartBody.Part.createFormData("file", item.imageFile!!.name, fileBody)

        val textBody = "Sample Text".toRequestBody("text/plain".toMediaTypeOrNull())

        retrofitClient.uploadFile("/", filePart, textBody)
            .subscribe(object : Observer<UploadImage?> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(t: UploadImage?) {
                    t?.let {
                        if (t.success) {
                            item.synced = "yes"
                            appDB.imageDao().updateImage(item)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
//                    uploadErrorLiveData.value = "something wrong"
                }

                override fun onComplete() {
//                    uploadFinishLiveData.value = cnt
//                    cnt++
                    if (uploadImageIndex != items.size - 1) {
                        uploadImageIndex++
                        uploadFile(items[uploadImageIndex])
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationManager.deleteNotificationChannel(CHANNEL_ID)
                            stopForeground(true)
                        } else stopSelf()
                    }
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
        notificationBuilder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Uploading...")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setAutoCancel(false)
            .setProgress(100, 0, false)
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationID, notification)
        startForeground(notificationID, notification)
    }

    private var uploadImageIndex: Int = 0
    private var notificationID: Int = 1001
    private val appDB: AppDatabase
        get() = AppDatabase.getInstance(applicationContext)

    private val retrofitClient: ApiInterface
        get() = RetrofitClient.getInstance().create(ApiInterface::class.java)

    override fun onProgressUpdate(currentPercent: Int) {
        notificationBuilder.setProgress(100, currentPercent, false)
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationID, notification)
    }
}