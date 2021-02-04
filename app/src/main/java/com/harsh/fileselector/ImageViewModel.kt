package com.harsh.fileselector

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ImageViewModel : ViewModel() {

    val images = MutableLiveData<List<ImageItem>>()

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
}