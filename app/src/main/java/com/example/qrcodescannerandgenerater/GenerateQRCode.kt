package com.example.qrcodescannerandgenerater

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

class GenerateQRCode : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Compose UI content goes here
            QRCodeGeneratorScreen()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun QRCodeGeneratorScreen() {
    // State for input text and QR bitmap
    var inputText by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current  // Get context within composable

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Display the generated QR code or default image
        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 75.dp)
            )
        } ?: run {
            // Default placeholder image from drawable resources
            Image(
                painter = painterResource(id = R.drawable.qrcode), // Placeholder image from resources
                contentDescription = "QR Code Placeholder",
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 75.dp)
            )
        }

        // Input and Button aligned slightly below the center
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 300.dp), // Adjust the top padding to move below center
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter your message") },
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 35.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row to align the Generate QR and Share QR buttons horizontally
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Generate QR Code Button
                Button(
                    onClick = {
                        qrBitmap = generateQRCode(inputText)  // Generate QR code on button click
                    },
                    modifier = Modifier.padding(end = 8.dp) // Add space between the buttons
                ) {
                    Text("Generate QR Code")
                }

                // Share QR Code Button
                Button(
                    onClick = {
                        qrBitmap?.let { bitmap ->
                            shareQRCode(context, bitmap) // Pass context to shareQRCode
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp) // Add space between the buttons
                ) {
                    Text("Share QR Code")
                }
            }
        }
    }
}

// Function to generate QR Code Bitmap
fun generateQRCode(text: String): Bitmap? {
    return try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Function to share QR Code as an image
fun shareQRCode(context: Context, bitmap: Bitmap) {
    val uri = bitmapToUri(context, bitmap)  // Convert the bitmap to URI
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri) // Attach the QR code image
        putExtra(Intent.EXTRA_TEXT, "Here is my QR code!") // Optional: Add text to share
    }
    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
}

// Function to convert Bitmap to Uri
fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "qr_code_image.png")
    try {
        val outStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        outStream.flush()
        outStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return FileProvider.getUriForFile(context, "com.example.qrcodescannerandgenerater.fileprovider", file)
}