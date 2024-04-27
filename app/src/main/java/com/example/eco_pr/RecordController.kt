import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import kotlin.math.log10

class RecordController(private val context: Context) {

    private var audioRecorder: MediaRecorder? = null

    fun start() {
        Log.d(TAG, "Start")
        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(getAudioPath())
            prepare()
            start()
        }
    }

    fun getDecibels(): Double {
        val amplitude = getVolume()
        return if (amplitude > 0) {
            20 * log10(amplitude / MAX_AMPLITUDE)
        } else {
            0.0
        }
    }

    private fun getAudioPath(): String {
        return "${context.cacheDir.absolutePath}${File.separator}${System.currentTimeMillis()}.wav"
    }

    fun stop() {
        audioRecorder?.let {
            Log.d(TAG, "Stop")
            it.stop()
            it.release()
        }
        audioRecorder = null
    }

    fun isAudioRecording() = audioRecorder != null

    fun getVolume() = audioRecorder?.maxAmplitude ?: 0

    companion object {
        private val TAG = RecordController::class.java.name
        private const val MAX_AMPLITUDE = 32768.0
    }
}
