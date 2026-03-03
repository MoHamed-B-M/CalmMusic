package com.music.calmplayer.utils

import android.content.Context
import android.provider.MediaStore
import java.io.File

object MediaLibraryHelper {
    fun getMusicFolders(context: Context): List<String> {
        val folderSet = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null, null, null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                val file = File(path)
                file.parent?.let { folderSet.add(it) }
            }
        }
        return folderSet.toList()
    }
}