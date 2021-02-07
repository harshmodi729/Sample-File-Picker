package com.harsh.fileselector.base

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val mFile: File,
    private val fileUploaderCallback: FileUploaderCallback
) :
    RequestBody() {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

    override fun contentType(): MediaType? {
        return "image/*".toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        val fileLength = mFile.length()
        val buffer =
            ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(mFile)
        var uploaded: Long = 0

        fileInputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { read = it } != -1) {
                handler.post {
                    fileUploaderCallback.onProgressUpdate((100 * uploaded / fileLength).toInt())
                }
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }

    interface FileUploaderCallback {
        fun onProgressUpdate(currentPercent: Int)
    }
}