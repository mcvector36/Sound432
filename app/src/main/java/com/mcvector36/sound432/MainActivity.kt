package com.mcvector36.sound432

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mcvector36.sound432.ui.theme.Sound432Theme
import java.io.File

class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 100
    private var permissionGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionGranted = checkStoragePermission()

        setContent {
            Sound432Theme {
                PermissionScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificăm din nou permisiunea după ce utilizatorul revine din setări
        permissionGranted = checkStoragePermission()
    }

    @Composable
    fun PermissionScreen() {
        val context = LocalContext.current
        var files by remember { mutableStateOf(listOf<String>()) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            Text(text = "Aplicația Sound432", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            if (permissionGranted) {
                Text("Permisiunea este acordată!", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { files = listFilesFromStorage() }) {
                    Text("Afișează fișierele")
                }
                Spacer(modifier = Modifier.height(16.dp))
                files.forEach { file ->
                    Text(text = file)
                }
            } else {
                Text("Permisiunea nu este acordată!", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { requestStoragePermission() }) {
                    Text("Solicită permisiunea")
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Settings.canDrawOverlays(this)
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Nu s-a putut deschide setările pentru permisiuni.", Toast.LENGTH_SHORT).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun listFilesFromStorage(): List<String> {
        val path = "/storage/emulated/0/"  // Folderul principal al memoriei interne
        val directory = File(path)
        val files = directory.listFiles()

        return files?.map { it.name } ?: listOf("Nicio fișier găsit!")
    }
}
