package com.example.lab_5_5.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class QrCodeAnalyzer(
    private val barcodeScanner: BarcodeScanner
) : ImageAnalysis.Analyzer {

    // Создаем поле с изменяемым SharedFlow, содержащий информацию о контенте QR-кода
    private val _currentQr = MutableSharedFlow<String>(
        extraBufferCapacity = 1, // количество элементов, которые будут забуферезированы, если читатели не успевают прочесть
        onBufferOverflow = BufferOverflow.DROP_OLDEST // в случае, если читателей нет, или они не успевают прочесть — убираем старые элементы
    )
    val currentQr = _currentQr.asSharedFlow()

    override fun analyze(image: ImageProxy) {
        // Так как нам необходимо закрывать image после использования,
        // воспользуемся функцией-расширением для AutoCloseable — .use
        image.use {
            val mediaImage = it.image ?: return@use
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                it.imageInfo.rotationDegrees
            )

            // Так как мы используем для анализатора отдельный поток (Executors.newSingleThreadExecutor),
            // то можем блокировать поток для обработки изображения
            runBlocking {
                val barcodes = barcodeScanner.process(inputImage).await() // дожидаемся обработки изображения
                val barcode = barcodes.firstOrNull()
                    ?: return@runBlocking // пытаемся получить первый элемент. если список пустой — выходим
                _currentQr.tryEmit(
                    barcode.rawValue ?: "Unknown value"
                ) // пытаемся отправить элемент. Если не удалось, то не блокируемся
                Log.d("QR", "Barcodes: $barcodes")
            }
        }
    }
}