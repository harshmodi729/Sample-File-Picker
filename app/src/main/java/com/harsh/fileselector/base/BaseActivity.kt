package com.harsh.fileselector.base

import androidx.appcompat.app.AppCompatActivity
import com.harsh.fileselector.data.ApiInterface
import com.harsh.fileselector.data.AppDatabase
import com.harsh.fileselector.data.RetrofitClient

open class BaseActivity : AppCompatActivity() {

    val appDB
        get() = AppDatabase.getInstance(this)

    val retrofitClient
        get() = RetrofitClient.getInstance().create(ApiInterface::class.java)
}