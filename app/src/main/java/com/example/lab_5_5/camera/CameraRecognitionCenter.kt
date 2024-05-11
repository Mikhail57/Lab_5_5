package com.example.lab_5_5.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class CameraRecognitionCenter(
    private val context: Context,
    private val qrCodeAnalyzer: QrCodeAnalyzer,
    private val cameraSaver: CameraSaver,
) : CameraProvider {
    private val _cameraProvider = MutableStateFlow<ProcessCameraProvider?>(null)
    override val cameraProvider: StateFlow<ProcessCameraProvider?>
        get() = _cameraProvider.asStateFlow()

    fun setupCamera(lifecycleOwner: LifecycleOwner, previewUseCase: UseCase) {
        lifecycleOwner.lifecycleScope.launch {
            // Для использования анализатором мы должны сначала создать UseCase, в котором он будет работать
            val imageAnalysis = ImageAnalysis.Builder().build()
            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(), // анализатор будет работать в отдельном новом потоке
                qrCodeAnalyzer,
            )

            // Экземпляр UseCase, использующийся для получения изображения, которое можно сохранить
            val imageCapture = ImageCapture.Builder().build()

            // Подписываемся на изменения видимого QR-кода
            qrCodeAnalyzer.currentQr
                .distinctUntilChanged() // дальше цепь выполняется, только если значение отличается
                .onEach { cameraSaver.savePhoto(imageCapture) } // пытаемся сохранить каждый элемент
                .launchIn(this) // запускаем Job в рамках текущего контекста

            val cameraProvider = withContext(Dispatchers.IO) {
                ProcessCameraProvider
                    .getInstance(context)
                    .await()
            }
            _cameraProvider.emit(cameraProvider)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageAnalysis,
                imageCapture,
            )
        }
    }
}