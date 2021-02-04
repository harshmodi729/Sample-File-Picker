package com.harsh.fileselector

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(item: ImageItem)

    @Update
    suspend fun updateImage(item: ImageItem)

    @Query("SELECT * FROM image_item where synced = :status")
    suspend fun getStatusWiseImage(status: String): List<ImageItem>
}