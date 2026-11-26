package com.resqsync.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
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
        if (!audioDir.exists()) audioDir.mkdirs()

        // Force 3GP format for all recordings to ensure file is under the 32KB Nearby Connections limit
        outputFile = File(audioDir, "sos_voice_${System.currentTimeMillis()}.3gp")

        // Primary attempt with AudioSource.MIC and 3GP/AMR-NB
        var success = setupAndStartRecorder(MediaRecorder.AudioSource.MIC, MediaRecorder.OutputFormat.THREE_GPP, MediaRecorder.AudioEncoder.AMR_NB, maxDurationMs)

        // Fallback attempt with AudioSource.DEFAULT if MIC failed
        if (!success) {
            Log.w("ResQSyncAudio", "Retrying MediaRecorder with AudioSource.DEFAULT fallback...")
            success = setupAndStartRecorder(MediaRecorder.AudioSource.DEFAULT, MediaRecorder.OutputFormat.THREE_GPP, MediaRecorder.AudioEncoder.AMR_NB, maxDurationMs)
        }

        return if (success) outputFile else null
    }

    private fun setupAndStartRecorder(source: Int, format: Int, encoder: Int, maxDurationMs: Long): Boolean {
        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(source)
                setOutputFormat(format)
                setAudioEncoder(encoder)

                // AMR-NB compresses audio enough to fit inside the strict 32KB limit of Google Nearby Connections payload
                if (encoder == MediaRecorder.AudioEncoder.AMR_NB) {
                    setAudioEncodingBitRate(12200) // 12.2 kbps
                    setAudioSamplingRate(8000)     // 8 kHz
                }

                setOutputFile(outputFile?.absolutePath)
                setMaxDuration(maxDurationMs.toInt())
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            Log.d("ResQSyncAudio", "Started ultra-compressed recording audio SOS memo (source=$source, format=$format)...")
            true
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Failed setupAndStartRecorder (source=$source, format=$format)", e)
            try {
                mediaRecorder?.release()
            } catch (ignored: Exception) {}
            mediaRecorder = null
            isRecording = false
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return outputFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error stopping recording", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return outputFile
    }

    fun startPlayback(fileToPlay: File? = outputFile, onComplete: () -> Unit = {}) {
        val targetFile = fileToPlay ?: outputFile ?: return
        if (!targetFile.exists() || targetFile.length() == 0L) {
            Log.e("ResQSyncAudio", "Cannot play audio: file is null, empty, or missing (${targetFile.absolutePath})")
            return
        }

        stopPlayback()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val originalMode = audioManager.mode

        @Suppress("DEPRECATION")
        val originalSpeakerphone = audioManager.isSpeakerphoneOn

        try {
            // Force audio routing to the bottom loudspeaker (Media/Music)
            audioManager.mode = AudioManager.MODE_NORMAL
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = true
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        } catch (e: Exception) {
            Log.w("ResQSyncAudio", "Could not force speakerphone volume", e)
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(targetFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    this@AudioRecorderHelper.isPlaying = false

                    // Revert speakerphone state after playback finishes
                    try {
                        @Suppress("DEPRECATION")
                        audioManager.isSpeakerphoneOn = originalSpeakerphone
                        audioManager.mode = originalMode
                    } catch (e: Exception) {
                        // ignore
                    }

                    onComplete()
                }
                start()
            }
            mediaPlayer = player
            isPlaying = true
            Log.d("ResQSyncAudio", "Loudspeaker playback started for ${targetFile.name} (${targetFile.length()} bytes)")
        } catch (e: Exception) {
            Log.e("ResQSyncAudio", "Error playing recording file: ${targetFile.absolutePath}", e)
            stopPlayback()
        }
    }

    fun stopPlayback() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_NORMAL

            mediaPlayer?.apply {
                if (this@AudioRecorderHelper.isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
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
