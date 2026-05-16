package com.freewind.clickopenfiledemo.domain.handler

import android.content.ActivityNotFoundException
import android.content.Intent
import com.freewind.clickopenfiledemo.domain.store.AppStore
import com.freewind.clickopenfiledemo.infra.system.AndroidFileSystemApi

class AppHandler(
    private val store: AppStore,
    private val systemApi: AndroidFileSystemApi,
) {
    fun createPickFileIntent(): Intent {
        return systemApi.createPickFileIntent()
    }

    fun onFilePicked(resultIntent: Intent?) {
        val pickedFile = systemApi.readPickedFile(resultIntent)
        if (pickedFile == null) {
            store.setStatus("未选中文件。")
            return
        }

        store.setSelectedFile(pickedFile)
    }

    fun openSelectedFile() {
        val selectedFile = store.state.value.selectedFile
        if (selectedFile == null) {
            store.setStatus("还没选文件。")
            return
        }

        try {
            systemApi.openWithDefaultApp(selectedFile)
            store.setStatus("已交给系统处理 ${selectedFile.displayName}")
        } catch (_: ActivityNotFoundException) {
            store.setStatus("系统里没找到能打开它的 app。")
        } catch (_: SecurityException) {
            store.setStatus("系统拒绝访问这个文件。重选一次再试。")
        }
    }
}
