package com.example.lab_5_5.camera

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.example.lab_5_5.postOnUiThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class CameraSaver(
    private val context: Context,
) {
    // сохраняем во «внешнее» хранилище приложения на sdcard
    private val outputDir by lazy { context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) }

    fun savePhoto(imageCapture: ImageCapture) {
        val photoFile = File(
            outputDir,
            // Формируем имя файла — время.jpg
            SimpleDateFormat("HH-mm-ss-SSS", Locale.US).format(Date()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Так как мы не можем показывать Toast не из главного потока,
                    // то используем самописный запускатор на главном потоке
                    postOnUiThread {
                        Toast.makeText(context, "Image saved ${photoFile.path}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    postOnUiThread {
                        Toast.makeText(
                            context,
                            "Image not saved: ${exception.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }


}