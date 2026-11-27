package com.resqsync.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var playbackCompletion: (() -> Unit)? = null
    var outputFile: File? = null
        private set

    var isRecording = false
        private set
    var isPlaying = false
        private set

    fun startRecording(maxDurationMs: Long = 10000L): File? {
        stopRecording()
        stopPlayback()

        val audioDir = File(context.cacheDir, "audio_sos")
        if (!audioDir.exists() && !audioDir.mkdirs()) {
            Log.e("ResQSyncAudio", "Unable to create audio cache directory: ${audioDir.absolutePath}")
            return null
        }

        val format = RecorderFormat(
            MediaRecorder.OutputFormat.THREE_GPP,
            MediaRecorder.AudioEncoder.AMR_NB,
            "3gp"
        )
        outputFile = File(audioDir, "sos_voice_${System.currentTimeMillis()}.${format.extension}")
        if (setupAndStartRecorder(MediaRecorder.AudioSource.MIC, format, maxDurationMs)) {
            return outputFile
        }

        Log.w("ResQSyncAudio", "Retrying 3gp recording with AudioSource.DEFAULT...")
        return if (setupAndStartRecorder(MediaRecorder.AudioSource.DEFAULT, format, maxDurationMs)) {
            outputFile
        } else {
            null
        }
    }

    private fun setupAndStartRecorder(
        source: Int,
        format: RecorderFormat,
        maxDurationMs: Long
    ): Boolean {
        var recorder: MediaRecorder? = null
        return try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            val activeRecorder = recorder ?: throw IllegalStateException("Recorder initialization failed")
            activeRecorder.apply {
                setAudioSource(source)
                setOutputFormat(format.outputFormat)
                setAudioEncoder(format.audioEncoder)
                setAudioEncodingBitRate(12200)
                setAudioSamplingRate(8000)

                setOutputFile(outputFile?.absolutePath)
                setMaxDuration(maxDurationMs.toInt())
                prepare()
                start()
            }

            mediaRecorder = activeRecorder
            isRecording = true
            Log.d(
                "ResQSyncAudio",
                "Started voice recording (source=$source, format=${format.extension}, bitrate=12200, sampleRate=8000)"
            )
            true
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Failed to start ${format.extension} recorder (source=$source)", e)
            try {
                recorder?.release()
            } catch (releaseError: Exception) {
                Log.w("ResQSyncAudio", "Failed to release recorder after setup error", releaseError)
            }
            mediaRecorder = null
            isRecording = false
            false
        }
    }

    private data class RecorderFormat(
        val outputFormat: Int,
        val audioEncoder: Int,
        val extension: String
    )

    fun stopRecording(): File? {
        if (!isRecording) return outputFile
        val recorder = mediaRecorder
        mediaRecorder = null
        isRecording = false
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error stopping recording", e)
        }
        try {
            recorder?.release()
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error releasing recorder", e)
        }
        val recordedFile = outputFile
        return recordedFile?.takeIf { it.isFile && it.length() > 0L }.also {
            if (it == null) {
                Log.e("ResQSyncAudio", "Recording produced no usable audio file")
            }
        }
    }

    fun startPlayback(fileToPlay: File? = outputFile, onComplete: () -> Unit = {}) {
        val targetFile = fileToPlay ?: outputFile ?: return
        if (!targetFile.exists() || targetFile.length() == 0L) {
            Log.e("ResQSyncAudio", "Cannot play audio: file is null, empty, or missing (${targetFile.absolutePath})")
            onComplete()
            return
        }

        startPlayback(
            description = targetFile.name,
            setDataSource = { player -> player.setDataSource(targetFile.absolutePath) },
            onComplete = onComplete
        )
    }

    fun startPlayback(audioBytes: ByteArray, onComplete: () -> Unit = {}) {
        if (audioBytes.isEmpty()) {
            Log.e("ResQSyncAudio", "Cannot play audio: byte array is empty")
            onComplete()
            return
        }

        val dataSource = object : MediaDataSource() {
            override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                if (position >= audioBytes.size) return -1
                val count = minOf(size, audioBytes.size - position.toInt())
                System.arraycopy(audioBytes, position.toInt(), buffer, offset, count)
                return count
            }

            override fun getSize(): Long = audioBytes.size.toLong()

            override fun close() = Unit
        }

        startPlayback(
            description = "decoded audio memo",
            setDataSource = { player -> player.setDataSource(dataSource) },
            onComplete = onComplete
        )
    }

    private fun startPlayback(
        description: String,
        setDataSource: (MediaPlayer) -> Unit,
        onComplete: () -> Unit
    ) {
        stopPlayback()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener(::onAudioFocusChange)
            .build()

        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w("ResQSyncAudio", "Audio focus was not granted; playback skipped")
            onComplete()
            return
        }
        audioFocusRequest = focusRequest
        playbackCompletion = onComplete

        var player: MediaPlayer? = null
        try {
            val activePlayer = MediaPlayer()
            player = activePlayer
            mediaPlayer = activePlayer
            activePlayer.setAudioAttributes(audioAttributes)
            setDataSource(activePlayer)
            activePlayer.setOnCompletionListener {
                releasePlayback(activePlayer)
            }
            activePlayer.setOnErrorListener { _, what, extra ->
                Log.e("ResQSyncAudio", "MediaPlayer error while playing $description (what=$what, extra=$extra)")
                releasePlayback(activePlayer)
                true
            }
            activePlayer.prepare()
            activePlayer.start()
            isPlaying = true
            Log.d("ResQSyncAudio", "Playback started for $description")
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error playing $description", e)
            releasePlayback(player)
        }
    }

    fun stopPlayback() {
        releasePlayback(mediaPlayer)
    }

    private fun onAudioFocusChange(focusChange: Int) {
        val player = mediaPlayer ?: return
        try {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> stopPlayback()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.pause()
                AudioManager.AUDIOFOCUS_GAIN -> if (isPlaying) player.start()
            }
        } catch (e: IllegalStateException) {
            Log.w("ResQSyncAudio", "Unable to update playback after audio focus change", e)
        }
    }

    private fun releasePlayback(player: MediaPlayer?) {
        if (mediaPlayer === player) {
            mediaPlayer = null
        }
        if (player != null) {
            try {
                player.release()
            } catch (e: IllegalStateException) {
                Log.w("ResQSyncAudio", "Failed to release MediaPlayer", e)
            }
        }
        isPlaying = false

        val request = audioFocusRequest
        if (request != null) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.abandonAudioFocusRequest(request)
            audioFocusRequest = null
        }
        val completion = playbackCompletion
        playbackCompletion = null
        completion?.invoke()
    }

    fun getAudioBase64(fileToEncode: File? = outputFile): String? {
        val targetFile = fileToEncode ?: outputFile ?: return null
        if (!targetFile.exists() || targetFile.length() == 0L) return null
        return try {
            val bytes = FileInputStream(targetFile).use { it.readBytes() }
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error encoding audio to Base64", e)
            null
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
