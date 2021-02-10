package com.harsh.fileselector.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fileselector.base.ProgressRequestBody
import com.harsh.fileselector.data.ApiInterface
import com.harsh.fileselector.data.AppDatabase
import com.harsh.fileselector.model.ImageItem
import com.harsh.fileselector.model.UploadImage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImageViewModel : ViewModel(), ProgressRequestBody.FileUploaderCallback {

    val images = MutableLiveData<List<ImageItem>>()
    val uploadProgressLiveData = MutableLiveData<Int>()
    val uploadFinishLiveData = MutableLiveData<Int>()
    val uploadErrorLiveData = MutableLiveData<String>()
    private var cnt = 1
    private var uploadImageIndex = 0
    private var items = ArrayList<ImageItem>()
    private lateinit var appDB: AppDatabase
    private lateinit var retrofitClient: ApiInterface

    fun insertImage(appDB: AppDatabase, item: ImageItem) {
        viewModelScope.launch {
            try {
                appDB.imageDao().insertImage(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateImage(appDB: AppDatabase, item: ImageItem) {
        viewModelScope.launch {
            try {
                appDB.imageDao().updateImage(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getImages(appDB: AppDatabase, status: String) {
        viewModelScope.launch {
            try {
                images.value = appDB.imageDao().getStatusWiseImage(status)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uploadImage(appDB: AppDatabase, retrofitClient: ApiInterface, items: ArrayList<ImageItem>) {
        viewModelScope.launch {
            try {
                var totalFileLength = 0L
                items.forEach {
                    totalFileLength += it.imageFile!!.length()
                }
                this@ImageViewModel.appDB = appDB
                this@ImageViewModel.retrofitClient = retrofitClient
                this@ImageViewModel.items = items
                uploadImageIndex = 0
                if (items.isNotEmpty()) {
                    uploadFile(items[uploadImageIndex])
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadFile(item: ImageItem) {
        val fileBody = ProgressRequestBody(item.imageFile!!, this@ImageViewModel)
        val filePart =
            MultipartBody.Part.createFormData("file", item.imageFile!!.name, fileBody)

        val textBody = "Sample Text".toRequestBody("text/plain".toMediaTypeOrNull())

        retrofitClient.uploadFile("/", filePart, textBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UploadImage?> {
                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(t: UploadImage?) {
                    t?.let {
                        if (t.success) {
                            item.synced = "yes"
                            updateImage(appDB, item)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    uploadErrorLiveData.value = "something wrong"
                }

                override fun onComplete() {
                    uploadFinishLiveData.value = cnt
                    cnt++
                    if (uploadImageIndex != items.size - 1) {
                        uploadImageIndex++
                        uploadFile(items[uploadImageIndex])
                    }
                }
            })
    }

    override fun onProgressUpdate(currentPercent: Int) {
        uploadProgressLiveData.value = currentPercent
    }
}