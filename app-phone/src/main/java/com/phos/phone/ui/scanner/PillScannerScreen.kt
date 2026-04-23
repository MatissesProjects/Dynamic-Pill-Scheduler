package com.phos.phone.ui.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

enum class ScanMode { PILL, BOTTLE }

@Composable
fun PillScannerScreen(
    onPillScanned: (PillScanResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val scannerEngine = remember { PillScannerEngine() }
    var scanMode by remember { mutableStateOf(ScanMode.BOTTLE) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("PillScanner", "Use case binding failed", e)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Mode Toggle
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .width(240.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            TabRow(
                selectedTabIndex = scanMode.ordinal,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = scanMode == ScanMode.PILL, 
                    onClick = { scanMode = ScanMode.PILL }
                ) {
                    Text("Pill", Modifier.padding(12.dp))
                }
                Tab(
                    selected = scanMode == ScanMode.BOTTLE, 
                    onClick = { scanMode = ScanMode.BOTTLE }
                ) {
                    Text("Bottle", Modifier.padding(12.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (scanMode == ScanMode.PILL) "Center the pill" else "Center the bottle label",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    capture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmapLocal()
                                scope.launch {
                                    val result = if (scanMode == ScanMode.BOTTLE) {
                                        scannerEngine.recognizeBottleText(bitmap)
                                    } else {
                                        scannerEngine.analyzePill(bitmap)
                                    }
                                    onPillScanned(result)
                                }
                                image.close()
                            }
                        }
                    )
                },
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    if (scanMode == ScanMode.PILL) Icons.Default.Medication else Icons.Default.QrCodeScanner,
                    contentDescription = "Scan",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun ImageProxy.toBitmapLocal(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
