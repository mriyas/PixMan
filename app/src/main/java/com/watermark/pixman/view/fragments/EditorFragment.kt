package com.watermark.pixman.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.watermark.pixman.R
import com.watermark.pixman.databinding.FragmentEditorBinding
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.watermark.pixman.view.PixManImageView
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream


class EditorFragment : Fragment() {
    val TAG = javaClass.simpleName

    lateinit var  iv_preview: PixManImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_editor, container, false
        ) as FragmentEditorBinding
        binding.fragment = this
        val view = binding.getRoot()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val path = arguments?.getString("filePath")


        iv_preview = view.findViewById<PixManImageView>(R.id.iv_preview)
       iv_preview.setImage(path)

    }

    fun opacityClick(view: View) {

        iv_preview.reduceOpcityTo50Percentage()

    }
    fun addTextClick(view: View) {
        iv_preview. addGreedyGameText()


    }
    fun verticalFlipClick(view: View){
       iv_preview.verticalFlip()
    }

    fun horizontalFlipClick(view: View){
       iv_preview.horizontalFlip()
    }
    fun undoClick(view: View){
        iv_preview.undo()
    }

    fun saveClick(view: View) {
        val thread = Thread(Runnable {
            if(iv_preview.bitmap==null){
                activity?.runOnUiThread(Runnable {
                    Toast.makeText(activity,"File could not be saved!!",Toast.LENGTH_LONG).show()
                })
                return@Runnable
            }
            val bytes = ByteArrayOutputStream()
            iv_preview.bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            val f = File(
                (Environment.getExternalStorageDirectory()).toString()
                        + File.separator + "pixman.png"
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()

            val path=f.absolutePath
            Log.d(TAG,"file Path : "+path)

            activity?.runOnUiThread(Runnable {
                Toast.makeText(activity,"File saved at $path",Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            })
        })
        thread.start()

    }


}