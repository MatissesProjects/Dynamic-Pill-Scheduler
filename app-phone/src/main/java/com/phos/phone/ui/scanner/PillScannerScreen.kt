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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
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
import com.phos.core.data.model.NutrientFacts
import kotlinx.coroutines.launch

enum class ScanMode { PILL, BOTTLE, FOOD, LABEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillScannerScreen(
    onPillScanned: (PillScanResult) -> Unit,
    onFoodScanned: (FoodScanResult) -> Unit = {},
    aiTextParser: (suspend (String) -> NutrientFacts?)? = null,
    aiVisionParser: (suspend (Bitmap) -> FoodScanResult?)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val scannerEngine = remember { PillScannerEngine() }
    val foodEngine = remember { FoodScannerEngine() }
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
                .width(320.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            TabRow(
                selectedTabIndex = scanMode.ordinal,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(selected = scanMode == ScanMode.PILL, onClick = { scanMode = ScanMode.PILL }) { Text("Pill", Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall) }
                Tab(selected = scanMode == ScanMode.BOTTLE, onClick = { scanMode = ScanMode.BOTTLE }) { Text("Bottle", Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall) }
                Tab(selected = scanMode == ScanMode.FOOD, onClick = { scanMode = ScanMode.FOOD }) { Text("Food", Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall) }
                Tab(selected = scanMode == ScanMode.LABEL, onClick = { scanMode = ScanMode.LABEL }) { Text("Label", Modifier.padding(12.dp), style = MaterialTheme.typography.labelSmall) }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when(scanMode) {
                    ScanMode.PILL -> "Center the pill"
                    ScanMode.BOTTLE -> "Center the bottle label"
                    ScanMode.FOOD -> "Center the food item"
                    ScanMode.LABEL -> "Center the nutrition facts"
                },
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
                                    when (scanMode) {
                                        ScanMode.BOTTLE -> onPillScanned(scannerEngine.recognizeBottleText(bitmap))
                                        ScanMode.PILL -> onPillScanned(scannerEngine.analyzePill(bitmap))
                                        ScanMode.FOOD -> onFoodScanned(foodEngine.identifyFood(bitmap, aiVisionParser))
                                        ScanMode.LABEL -> onFoodScanned(foodEngine.scanNutritionLabel(bitmap, aiTextParser))
                                    }
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
                    when(scanMode) {
                        ScanMode.PILL -> Icons.Default.Medication
                        ScanMode.BOTTLE -> Icons.Default.QrCodeScanner
                        ScanMode.FOOD -> Icons.Default.Restaurant
                        ScanMode.LABEL -> Icons.Default.Description
                    },
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
