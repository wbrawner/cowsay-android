package com.wbrawner.cowsay

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.content.FileProvider
import android.view.View
import com.github.ricksbrown.cowsay.Cowsay
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handlerThread = HandlerThread("io")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        preview_button.setOnClickListener {
            val cowMessage = Cowsay.say(arrayOf(message.text.toString()))
            preview_text.text = cowMessage
            share.visibility = View.VISIBLE
        }

        share.setOnClickListener {
            val width = preview_text.width
            val height = preview_text.height
            handler.post {
                val cowBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val cowCanvas = Canvas(cowBitmap)
                preview_text.draw(cowCanvas)
                val cowImage = File(filesDir, "images/cowsay.jpg")
                cowImage.parentFile.mkdirs()
                cowImage.outputStream().use { cowOut ->
                    cowBitmap.compress(Bitmap.CompressFormat.JPEG, 100, cowOut)
                }
                val fileUri = FileProvider.getUriForFile(this@MainActivity, "com.wbrawner.cowsay.fileprovider", cowImage)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    setDataAndType(
                        fileUri,
                        contentResolver.getType(fileUri)
                    )
                    putExtra(Intent.EXTRA_STREAM, fileUri)

                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runOnUiThread {
                    startActivity(shareIntent)
                }
            }
        }
    }
}
