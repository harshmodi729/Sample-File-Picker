package com.harsh.fileselector.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.harsh.fileselector.R
import com.harsh.fileselector.base.BaseActivity
import com.harsh.fileselector.model.ImageItem
import com.harsh.fileselector.viewmodel.ImageViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var viewModel: ImageViewModel
    private var uris = ArrayList<Uri>()
    private var alPaths = ArrayList<String>()
    private var alFiles = ArrayList<File>()
    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(ImageViewModel::class.java)

        viewModel.getImages(appDB, "not")

        btnOpenImage.setOnClickListener {
            checkStoragePermission()
        }

        btnUploadImage.setOnClickListener {
            isUploading = true
            viewModel.getImages(appDB, "not")
        }

        viewModel.images.observe(this,
            {
                it?.let { items ->
                    items.forEach { item ->
                        item.imageFile = File(item.path)
                    }
                    if (isUploading) {
                        viewModel.uploadImage(appDB, retrofitClient, items as ArrayList<ImageItem>)
                        /*val intent = Intent(this, UploadImageService::class.java)
                        intent.putExtra("files", items as ArrayList<ImageItem>)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(this, intent)
                        } else {
                            startService(intent)
                        }*/
                    } else {
                        alFiles.clear()
                        items.forEach { imagePath ->
                            alFiles.add(imagePath.imageFile!!)
                        }
                        tvPaths.text = items.size.toString()
                    }
                }
            })

        viewModel.uploadProgressLiveData.observe(this, {
            progress.progress = it
            Log.e("progress", it.toString())
        })
        viewModel.uploadFinishLiveData.observe(this, {
            if (it == alFiles.size) {
                isUploading = false
                viewModel.getImages(appDB, "not")
            }
            progress.progress = 0
            Log.e("finish", it.toString())
        })
        viewModel.uploadErrorLiveData.observe(this, {
            isUploading = false
            progress.progress = 0
            Log.e("error", it)
        })
    }

    private fun checkStoragePermission() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(response: MultiplePermissionsReport?) {
                    if (response?.areAllPermissionsGranted()!!) {
                        FilePickerBuilder.instance
                            .setMaxCount(5)
                            .setSelectedFiles(uris)
                            .pickPhoto(this@MainActivity)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "You have denied the permission.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (response.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(
                            this@MainActivity,
                            "You can not open image picker without this permission.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    uris =
                        data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                    alPaths.clear()
                    alFiles.clear()
                    uris.forEach {
                        ContentUriUtils.getFilePath(this, it)?.let { path ->
                            val file = File(path)
                            alFiles.add(file)
                            alPaths.add(path)
                        }
                    }
                }
            }
        }
        tvPaths.text = alPaths.size.toString()
        insertImages()
    }

    private fun insertImages() {
        for (i in 0 until alFiles.size) {
            val item = ImageItem(path = alPaths[i], synced = "not")
            item.imageFile = alFiles[i]
            viewModel.insertImage(appDB, item)
        }
    }
}