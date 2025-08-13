package com.example.soulscript.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun start() {
        val file = File(context.cacheDir, "audio_note_${System.currentTimeMillis()}.mp3")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        audioFile = file
    }

    fun stop(): String? {
        recorder?.stop()
        recorder?.release()
        recorder = null
        return audioFile?.absolutePath
    }
}

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(path: String, onCompletion: () -> Unit) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
            setOnCompletionListener {
                onCompletion()
            }
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}