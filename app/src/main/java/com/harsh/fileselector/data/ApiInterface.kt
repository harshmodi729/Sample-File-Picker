package com.harsh.fileselector.data

import com.harsh.fileselector.model.UploadImage
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface ApiInterface {

    @Multipart
    @POST
    fun uploadFile(
        @Url url: String,
        @Part file: MultipartBody.Part,
        @Part("text") text: RequestBody
    ): Observable<UploadImage>
}