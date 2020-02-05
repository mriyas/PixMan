package com.watermark.pixman.view

import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import java.util.*

class PixManImageView : AppCompatImageView {
    var bitmapAlpha = 100
    lateinit var bitmap: Bitmap
    lateinit var originalBitmap: Bitmap
    val stack:Stack<Bitmap> = Stack()



    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    private fun verticalFlip(pushToStack: Boolean){
        stack.push(bitmap)

        val cx = bitmap.width / 2f
        val cy = bitmap.height / 2f
        bitmap = bitmap.flip(1f, -1f, cx, cy)
        setImageBitmap(bitmap)


    }
    fun verticalFlip() {
       verticalFlip(true)
    }
    fun horizontalFlip(){
      horizontalFlip(true)

    }
   private fun horizontalFlip(pushToStack: Boolean){
       stack.push(bitmap)
       val cx = bitmap.width / 2f
        val cy = bitmap.height / 2f
        bitmap = bitmap.flip(-1f, 1f, cx, cy)
       setImageBitmap(bitmap)


    }
    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }



    private fun alphaBitmap(originalBitmap: Bitmap): Bitmap {
        // lets create a new empty bitmap
        val newBitmap = Bitmap.createBitmap(
            originalBitmap.getWidth(),
            originalBitmap.getHeight(),
            Bitmap.Config.ARGB_8888
        )
//
        val canvas = Canvas(newBitmap)

        val alphaPaint = Paint()
        alphaPaint.alpha = bitmapAlpha

        canvas.drawBitmap(originalBitmap, 0f, 0f, alphaPaint)
        return newBitmap

    }

    fun reduceOpcityTo50Percentage(){
        stack.push(bitmap)
        bitmapAlpha = bitmapAlpha/2
        if(bitmapAlpha<0){
            bitmapAlpha=0
        }
        bitmap=alphaBitmap(bitmap)
        setImageBitmap(bitmap)


    }
   private fun incrementOpcityTo50Percentage(){
        bitmapAlpha = bitmapAlpha*2
        if(bitmapAlpha>100){
            bitmapAlpha=100
        }
        bitmap=alphaBitmap(bitmap)
        setImageBitmap(bitmap)


    }
    fun addGreedyGameText(){

        stack.push(bitmap)
        bitmap = drawMultilineTextToBitmap()
        setImageBitmap(bitmap)

    }

    fun undo(){
        if(stack!=null&&stack.size<=0){
            return
        }
         bitmap=stack.get(stack.size-1)
        stack.pop()
        setImageBitmap()

    }
    fun setImage(path:String?):Bitmap{
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        var bitmap = BitmapFactory.decodeFile(path, options)



        var bitmapConfig: Bitmap.Config? = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888
        }

        bitmap = bitmap.copy(bitmapConfig, true)
        this.bitmap=bitmap
        this.originalBitmap= bitmap.copy(bitmapConfig, true)
        setImageBitmap()
        return bitmap
    }

     fun setImageBitmap() {
        super.setImageBitmap(bitmap)
    }




   private fun drawMultilineTextToBitmap(

    ): Bitmap {


        val canvas = Canvas(bitmap)
        val mTxtPaint = Paint()
        val str = "GreedyGame"
        val fm = Paint.FontMetrics()
        mTxtPaint.setColor(Color.BLACK)
        mTxtPaint.setTextSize(25.0f)

        mTxtPaint.getFontMetrics(fm)
        val textHeight = 20f

        val x = ((bitmap.width) / 2) * 1f - (mTxtPaint.measureText(str) / 2)
        val y = ((bitmap.height) / 2) * 1f + (textHeight / 2)

        val margin = 10
        //val x=100f
        //val y=200f
        canvas.drawRect(
            x - margin, y - (textHeight) - margin,
            x + mTxtPaint.measureText(str) + margin, y + margin, mTxtPaint
        )


        mTxtPaint.setColor(Color.GREEN)

        canvas.drawText(str, x, y, mTxtPaint)

        val alphaPaint = Paint()
        alphaPaint.alpha = bitmapAlpha
// now lets draw using alphaPaint instance
        canvas.drawBitmap(bitmap, 0f, 0f, alphaPaint)

        return bitmap
    }


}
