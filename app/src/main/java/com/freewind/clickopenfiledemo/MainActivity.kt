package com.freewind.clickopenfiledemo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freewind.clickopenfiledemo.domain.handler.AppHandler
import com.freewind.clickopenfiledemo.domain.store.AppState
import com.freewind.clickopenfiledemo.domain.store.AppStore
import com.freewind.clickopenfiledemo.infra.system.AndroidFileSystemApi

class MainActivity : ComponentActivity() {
    private val store = AppStore()
    private val handler by lazy {
        AppHandler(
            store = store,
            systemApi = AndroidFileSystemApi(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appState by store.state.collectAsStateWithLifecycle()
            val pickerLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handler.onFilePicked(result.data)
                } else {
                    handler.onFilePicked(null)
                }
            }

            MaterialTheme {
                FileOpenDemoScreen(
                    state = appState,
                    onPickFile = {
                        pickerLauncher.launch(handler.createPickFileIntent())
                    },
                    onOpenFile = handler::openSelectedFile,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileOpenDemoScreen(
    state: AppState,
    onPickFile: () -> Unit,
    onOpenFile: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("系统默认打开文件")
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "先用系统文件选择器挑一个文件，再直接交给系统默认 app 处理。图片、文本、APK、其他可识别文件都走系统原生行为。",
                style = MaterialTheme.typography.bodyLarge,
            )

            Button(
                onClick = onPickFile,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("选择文件")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.selectedFile?.displayName ?: "未选择文件",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "MIME: ${state.selectedFile?.mimeType ?: "未知，打开时按 */* 兜底"}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "URI: ${state.selectedFile?.uri ?: "无"}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(
                onClick = onOpenFile,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("系统默认打开")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = state.statusMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
