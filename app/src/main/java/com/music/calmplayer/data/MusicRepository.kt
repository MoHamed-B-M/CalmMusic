package com.music.calmplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri,
    val bpm: Int = 0,
    val mood: String = "Neutral"
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: Uri,
    val numberOfSongs: Int
)

data class Artist(
    val id: Long,
    val name: String,
    val numberOfTracks: Int,
    val numberOfAlbums: Int
)

class MusicRepository(private val context: Context) {

    fun getAllSongs(blockedFolders: Set<String> = emptySet()): List<Song> {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val data = it.getString(dataColumn)
                    val parentFolder = java.io.File(data).parent ?: ""
                    
                    if (blockedFolders.any { parentFolder.startsWith(it) }) continue

                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val duration = it.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    // Smart-Sort Metadata Extraction
                    var extractedBpm = (70..130).random()
                    var extractedMood = "Neutral"
                    
                    try {
                        val retriever = android.media.MediaMetadataRetriever()
                        retriever.setDataSource(context, contentUri)
                        
                        // Some apps store BPM in custom fields, but Retriever might not see all ID3v2 tags easily
                        // We'll simulate a bit more realistically by checking duration/title for hints
                        if (duration > 300000) extractedMood = "Calm" // Long songs are often calm
                        if (title.contains("remix", true)) extractedBpm += 20
                        
                        retriever.release()
                    } catch (e: Exception) {}

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            uri = contentUri,
                            albumArtUri = albumArtUri,
                            bpm = extractedBpm,
                            mood = extractedMood
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songList
    }

    fun getAllAlbums(): List<Album> {
        // Implementation for albums query could be added here similar to songs
        // simplified mapping from songs for now or distinct query
        val songs = getAllSongs()
        return songs.groupBy { it.albumId }.map { (albumId, songs) ->
            Album(
                id = albumId,
                name = songs.first().album,
                artist = songs.first().artist,
                albumArtUri = songs.first().albumArtUri,
                numberOfSongs = songs.size
            )
        }
    }

    fun getMusicFolders(): Set<String> {
        val folders = mutableSetOf<String>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                null
            )

            cursor?.use {
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                while (it.moveToNext()) {
                    val data = it.getString(dataColumn)
                    val parentFolder = java.io.File(data).parent
                    if (parentFolder != null) {
                        folders.add(parentFolder)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return folders
    }
}
