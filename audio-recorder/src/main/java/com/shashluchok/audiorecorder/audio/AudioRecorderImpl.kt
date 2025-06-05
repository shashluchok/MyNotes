package com.shashluchok.audiorecorder.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.shashluchok.audiorecorder.audio.codec.AudioEncoder
import com.shashluchok.audiorecorder.audio.codec.lame.LameEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs


class AudioRecorderImpl : AudioRecorder {

    private val audioMode = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val encoder: AudioEncoder = LameEncoder(SAMPLE_RATE)

    private val minBufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, audioMode, audioFormat
    )

    private val isProcessing = AtomicBoolean(false)

    @SuppressLint("MissingPermission")
    private var audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,
        audioMode,
        audioFormat,
        minBufferSize * 2
    )

    override suspend fun record(
        dataSource: FileDataSource,
        onStart: () -> Unit,
        onNewVolume: (Float) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            initAudioRecorder()
            audioRecord.startRecording()
            onStart()

            dataSource.openOutputStream(append = true).use {
                pcmToFile(it, onNewVolume).not()
            }
        }
    }

    override fun stop() {
        if (audioRecord.state == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop()
        }
        audioRecord.release()
        isProcessing.set(false)
    }

    override fun destroy() {
        audioRecord.release()
        isProcessing.set(false)
        encoder.close()
    }

    private fun pcmToFile(
        outputStream: OutputStream,
        onNewVolume: (Float) -> Unit
    ): Boolean {
        val pcmBuffer = ShortArray(BUFFER_SIZE * 2)
        val mp3Buffer = ByteArray(BUFFER_SIZE)
        isProcessing.set(true)
        try {
            var read: Int
            var write: Int
            while (isProcessing.get()) {
                read = audioRecord.read(pcmBuffer, 0, minBufferSize)
                if (read < 0) return false
                if (read != 0) {
                    onNewVolume(calculateVolumeLevel(pcmBuffer, read))
                    write = encoder.encode(pcmBuffer, read, mp3Buffer)
                    if (write > 0) outputStream.write(mp3Buffer, 0, write)
                }
            }
            write = encoder.flush()
            if (write > 0) {
                outputStream.write(mp3Buffer, 0, write)
            }
        } catch (e: IOException) {
            return false
        } finally {
            isProcessing.set(false)
            encoder.close()
        }
        return true
    }

    private fun calculateVolumeLevel(pcmBuffer: ShortArray, read: Int): Float {
        var max = 0f
        if (pcmBuffer.size >= read) {
            (0..read).onEach {
                var temp = abs(pcmBuffer[it].toFloat())
                if (temp > AMPLITUDE_THRESHOLD) {
                    temp = AMPLITUDE_THRESHOLD
                }
                if (max < temp) {
                    max = temp
                }
            }
        }

        return max / AMPLITUDE_THRESHOLD
    }

    @SuppressLint("MissingPermission")
    private fun initAudioRecorder() {
        if (audioRecord.state == AudioRecord.STATE_UNINITIALIZED) {

            val audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                audioMode,
                audioFormat,
                minBufferSize * 2
            )
            if (audioRecorder.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord = audioRecorder
                return
            }
        }
    }

    companion object {
        private const val SAMPLE_RATE= 44000
        private const val BUFFER_SIZE = 8_192
        private const val AMPLITUDE_THRESHOLD = 30000.0f
    }
}