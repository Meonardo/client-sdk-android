package io.livekit.android.room.track

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import androidx.core.content.ContextCompat
import org.webrtc.AudioBufferSource
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class SystemAudioCapturer(context: Context, private val captureConfiguration: AudioPlaybackCaptureConfiguration, internal val source: AudioBufferSource) {

    private val recorder: AudioRecord

    private val sampleRate = 48000

    private var bufferSize = 1920

    private var isRecording = AtomicBoolean(false)

    private var audioBuffer: ByteBuffer? = null

    init {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Record audio permissions are required to create an audio track.")
        }
        this.bufferSize = AudioRecord.getMinBufferSize(sampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT)
        this.recorder = AudioRecord.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                .build())
            .setAudioPlaybackCaptureConfig(captureConfiguration)
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    public fun start() {
        if (isRecording.get()) {
            return
        }

        // allocate buffer
        if (audioBuffer == null) {
            audioBuffer = ByteBuffer.allocateDirect(bufferSize)
        }

        recorder.startRecording()

        isRecording.set(true)
        run()
    }

    public fun stop() {
        if (!isRecording.get()) {
            return
        }
        recorder.stop()
        isRecording.set(false)
    }

    private fun run() {
        Thread {
            val buffer = ByteArray(bufferSize)
            while (isRecording.get()) {
                val read = recorder.read(buffer, 0, bufferSize)
                if (read < 0) {
                    break
                }
                audioBuffer?.put(buffer)

                // process audio buffer
                source.OnAudioBuffer(audioBuffer, sampleRate, 2, 0)

                // reset buffer
                audioBuffer?.clear()
            }
        }.start()
    }
}
