package com.harsh.fileselector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ImageViewModel
    private lateinit var appDB: AppDatabase
    private var uris = ArrayList<Uri>()
    private var alPaths = ArrayList<String>()
    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appDB = AppDatabase.getInstance(this)
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
                    if (isUploading) {
                        items.forEach { image ->
                            image.synced = "yes"
                            viewModel.updateImage(appDB, image)
                        }
                        isUploading = false
                        viewModel.getImages(appDB, "not")
                    } else {
                        tvPaths.text = items.size.toString()
                    }
                }
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
                    uris.forEach {
                        ContentUriUtils.getFilePath(this, it)?.let { path ->
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
        alPaths.forEach {
            viewModel.insertImage(appDB, ImageItem(path = it, synced = "not"))
        }
    }
}