package com.watermark.pixman.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.watermark.pixman.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private val EXTERNAL_STORAGE_REQUEST_CODE = 2000
    private val GALLERY_KITKAT_INTENT_CALLED = 101
    private val GALLERY_INTENT_CALLED = 100

    private lateinit var filePath: String

    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate(
            inflater, com.watermark.pixman.R.layout.fragment_home, container, false
        ) as FragmentHomeBinding
        binding.fragment = this
        val view = binding.getRoot()

        return view
    }

    fun onChooseImageClick(view: View) {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                EXTERNAL_STORAGE_REQUEST_CODE
            )


        } else {
            startGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            EXTERNAL_STORAGE_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(
                        activity,
                        "Permission has been denied by user",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    startGallery()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startGallery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                GALLERY_INTENT_CALLED
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var cursor: Cursor? = null
        if (resultCode != Activity.RESULT_OK)
            return
        if (data == null)
            return
        var originalUri: Uri? = null
        if (requestCode == GALLERY_INTENT_CALLED && data != null) {
            originalUri = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            cursor = activity!!.contentResolver.query(
                originalUri!!,
                filePathColumn, null, null, null
            )
            cursor!!.moveToFirst()

            val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
            filePath = cursor!!.getString(columnIndex)
            cursor!!.close()
            Log.d(TAG, "path of uri:" + filePath)

        } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED && data != null) {
            originalUri = data.data

            val takeFlags =
                data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // Check for the freshest data.
            activity!!.contentResolver.takePersistableUriPermission(
                originalUri!!, takeFlags
            )
            Log.d(TAG, " Uri: " + originalUri!!.toString())

            filePath = getPath(activity, originalUri)!!

            Log.d("filepath", filePath)

        }
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(activity, "Choose a proper image", Toast.LENGTH_LONG).show()
        } else {
            val actionDetail = HomeFragmentDirections.chooseFileAction(filePath = filePath)
            //  actionDetail.se(item)
            findNavController().navigate(actionDetail)
        }

    }

    @SuppressLint("NewApi")
    fun getPath(context: Context?, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    val data = Environment.getExternalStorageDirectory()
                    return (data.absolutePath + "/"
                            + split[1])
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )

                return getDataColumn(context!!, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(
                    context!!, contentUri, selection,
                    selectionArgs
                )
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
            return getDataColumn(context!!, uri, null, null)
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return uri.getPath()
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?,
        selection: String?, selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.getContentResolver().query(
                uri!!, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor!!.moveToFirst()) {
                val column_index = cursor!!.getColumnIndexOrThrow(column)
                return cursor!!.getString(column_index)
            }
        } finally {
            if (cursor != null)
                cursor!!.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }
}