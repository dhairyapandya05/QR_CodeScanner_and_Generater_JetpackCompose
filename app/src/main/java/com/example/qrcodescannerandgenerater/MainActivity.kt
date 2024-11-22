package com.example.qrcodescannerandgenerater

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.BackpressureStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.qrcodescannerandgenerater.ui.theme.QRCodeScannerAndGeneraterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRCodeScannerAndGeneraterTheme {
                var code by remember {
                    mutableStateOf("")
                }
                var context= LocalContext.current
                var lifecycleOwner = LocalLifecycleOwner.current
                var cameraProviderFuture=remember{
                    ProcessCameraProvider.getInstance(context)
                }
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )==PackageManager.PERMISSION_GRANTED
                    )
                }
                var launcher= rememberLauncherForActivityResult(
                    contract =ActivityResultContracts.RequestPermission(),
                    onResult = {granted->
                        hasCameraPermission=granted
                    }
                )
                LaunchedEffect(key1 = true){
                    launcher.launch(Manifest.permission.CAMERA)
                }
                Column(
                    modifier=Modifier.fillMaxSize()
                ) {
                    if(hasCameraPermission) {
                        AndroidView(
                            factory = {context->
                            val previewView= PreviewView(context)
                            val preview=androidx.camera.core.Preview.Builder().build()
                            val selector=CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                            val imageAnalyser=ImageAnalysis.Builder()
                                .setTargetResolution(Size(
                                    previewView.width,
                                    previewView.height
                                ))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            imageAnalyser.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                QrcodeAnalyser{ result->
                                    code=result
                                }
                            )
                            try {
                                cameraProviderFuture.get().bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageAnalyser
                                )
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                            }
                            previewView
                        },
                            modifier=Modifier.weight(1f)
                        )
                        Text(text = code,
                            fontSize = 20.sp,
                            modifier= Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                        Button(onClick = {
                            val intent= Intent(context,GenerateQRCode::class.java)
                            context.startActivity(intent)
                        }, modifier=Modifier.fillMaxWidth()
                            .padding(16.dp)
                        ){
                            Text(text = "Generate QR Code")
                        }
                    }
                }
            }
        }
    }
}

