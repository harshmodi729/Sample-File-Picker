package com.harsh.fileselector.model

import androidx.room.*
import java.io.File
import java.io.Serializable

@Entity(tableName = "image_item", indices = [Index(value = ["path"], unique = true)])
data class ImageItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "path")
    val path: String = "",
    @ColumnInfo(name = "synced")
    var synced: String = ""
) : Serializable {
    @Ignore
    var imageFile: File? = null
}