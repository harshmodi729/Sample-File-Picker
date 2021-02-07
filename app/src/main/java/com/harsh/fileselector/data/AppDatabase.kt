package com.harsh.fileselector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harsh.fileselector.model.ImageItem

@Database(entities = [ImageItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "file_picker_db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}