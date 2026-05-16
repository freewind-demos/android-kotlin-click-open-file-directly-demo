package com.freewind.clickopenfiledemo.domain.store

import com.freewind.clickopenfiledemo.domain.model.SelectedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppState(
    val selectedFile: SelectedFile? = null,
    val statusMessage: String = "先选文件，再点“系统默认打开”。",
)

class AppStore {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun setSelectedFile(file: SelectedFile) {
        _state.update {
            it.copy(
                selectedFile = file,
                statusMessage = "已选择 ${file.displayName}",
            )
        }
    }

    fun setStatus(message: String) {
        _state.update { it.copy(statusMessage = message) }
    }
}
