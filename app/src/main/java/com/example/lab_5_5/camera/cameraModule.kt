package com.example.lab_5_5.camera

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import org.koin.dsl.module

val cameraModule = module {
    single<BarcodeScanner> { BarcodeScanning.getClient() }
    single { QrCodeAnalyzer(barcodeScanner = get()) }
    single { CameraSaver(context = get()) }
    single {
        CameraRecognitionCenter(
            context = get(),
            qrCodeAnalyzer = get(),
            cameraSaver = get()
        )
    }
}