package com.harsh.fileselector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.harsh.fileselector.model.ImageItem

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(item: ImageItem)

    @Update
    fun updateImage(item: ImageItem)

    @Query("SELECT * FROM image_item where synced = :status")
    suspend fun getStatusWiseImage(status: String): List<ImageItem>
}