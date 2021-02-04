package com.harsh.fileselector

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_item")
data class ImageItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "path")
    val path: String = "",
    @ColumnInfo(name = "synced")
    var synced: String = ""
)