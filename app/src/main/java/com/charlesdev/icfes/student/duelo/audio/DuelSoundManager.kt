// DuelSoundManager.kt - VERSIÓN CON ARCHIVOS DE AUDIO PERSONALIZADOS
package com.charlesdev.icfes.student.duelo.audio

import android.Manifest
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.*
import com.charlesdev.icfes.R

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuelSoundManager private constructor(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var vibrator: Vibrator? = null
    private var isEnabled = true
    private var soundsLoaded = false

    // IDs de los sonidos cargados
    private val soundIds = mutableMapOf<String, Int>()

    companion object {
        private const val TAG = "DuelSoundManager"
        private const val MAX_STREAMS = 6

        @Volatile
        private var INSTANCE: DuelSoundManager? = null

        fun getInstance(context: Context): DuelSoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DuelSoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        initializeSoundPool()
        initializeVibrator()
        loadAllSounds()
    }

    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    Log.d(TAG, "Sonido cargado exitosamente")
                }
            }

            Log.d(TAG, "SoundPool creado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error creando SoundPool", e)
        }
    }

    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun loadAllSounds() {
        try {
            // Cargar todos los sonidos personalizados
            soundIds["correct_answer"] = soundPool?.load(context, R.raw.correct_answer,1)?: 0
            soundIds["wrong_answer"] = soundPool?.load(context, R.raw.wrong_answer, 1) ?: 0
            soundIds["time_warning"] = soundPool?.load(context, R.raw.time_warning, 1) ?: 0
            soundIds["time_up"] = soundPool?.load(context, R.raw.time_up, 1) ?: 0
            soundIds["question_appear"] = soundPool?.load(context, R.raw.question_appear, 1) ?: 0
            soundIds["game_start"] = soundPool?.load(context, R.raw.game_start, 1) ?: 0
            soundIds["player_join"] = soundPool?.load(context, R.raw.player_join, 1) ?: 0
            soundIds["victory"] = soundPool?.load(context, R.raw.victory, 1) ?: 0
            soundIds["tick"] = soundPool?.load(context, R.raw.tick, 1) ?: 0
            soundIds["button_click"] = soundPool?.load(context, R.raw.button_click, 1) ?: 0
            soundIds["notification"] = soundPool?.load(context, R.raw.notification, 1) ?: 0
            soundIds["perfect_score"] = soundPool?.load(context, R.raw.perfect_score, 1) ?: 0
            soundIds["team_balance"] = soundPool?.load(context, R.raw.team_balance, 1) ?: 0
            soundIds["countdown_1"] = soundPool?.load(context, R.raw.countdown_1, 1) ?: 0
            soundIds["countdown_2"] = soundPool?.load(context, R.raw.countdown_2, 1) ?: 0
            soundIds["countdown_3"] = soundPool?.load(context, R.raw.countdown_3, 1) ?: 0
            soundIds["countdown_4"] = soundPool?.load(context, R.raw.countdown_4, 1) ?: 0
            soundIds["countdown_5"] = soundPool?.load(context, R.raw.countdown_5, 1) ?: 0
            // Esperar un poco para que se carguen
            GlobalScope.launch(Dispatchers.Main) {
                delay(500)
                soundsLoaded = true
                Log.d(com.charlesdev.icfes.student.duelo.audio.DuelSoundManager.Companion.TAG, "Todos los sonidos cargados: ${soundIds.size} archivos")
            }

        } catch (e: Exception) {
            Log.e(com.charlesdev.icfes.student.duelo.audio.DuelSoundManager.Companion.TAG, "Error cargando sonidos", e)
        }
    }

    // MÉTODOS PÚBLICOS PARA REPRODUCIR SONIDOS

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playCorrectAnswer() {
        playSound("correct_answer", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 80, 40, 80, 40, 120), -1)
        Log.d(TAG, "Sonido respuesta correcta - melodía triunfal")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playWrongAnswer() {
        playSound("wrong_answer", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 200, 100, 200), -1)
        Log.d(TAG, "Sonido respuesta incorrecta - error")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playTimeWarning() {
        playSound("time_warning", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 50, 50, 50, 50, 50, 50, 50), -1)
        Log.d(TAG, "Sonido advertencia tiempo - urgente")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playTimeUp() {
        playSound("time_up", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 400, 150, 400, 150, 400), -1)
        Log.d(TAG, "Sonido tiempo agotado - dramático")
    }

    fun playQuestionAppear() {
        playSound("question_appear", 0.8f, 1.0f)
        Log.d(TAG, "Sonido nueva pregunta - suave")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playGameStart() {
        playSound("game_start", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 100, 80, 100, 80, 100, 80, 200), -1)
        Log.d(TAG, "Sonido inicio juego - fanfarria épica")
    }

    fun playPlayerJoin() {
        playSound("player_join", 0.7f, 1.0f)
        Log.d(TAG, "Sonido jugador se une - bienvenida")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playVictory() {
        playSound("victory", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 150, 100, 150, 100, 150, 100, 200, 100, 300), -1)
        Log.d(TAG, "Sonido victoria - melodía épica")
    }

    fun playTick() {
        playSound("tick", 0.5f, 1.0f)
        Log.d(TAG, "Sonido tick - sutil")
    }

    fun playButtonClick() {
        playSound("button_click", 0.6f, 1.0f)
        Log.d(TAG, "Sonido click botón - satisfactorio")
    }

    fun playNotification() {
        playSound("notification", 0.8f, 1.0f)
        Log.d(TAG, "Sonido notificación - amigable")
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playPerfectScore() {
        playSound("perfect_score", 1.0f, 1.0f)
        vibrate(longArrayOf(0, 150, 50, 150, 50, 150, 50, 300), -1)
        Log.d(TAG, "Sonido puntaje perfecto")
    }

    fun playTeamBalance() {
        playSound("team_balance", 0.7f, 1.0f)
        Log.d(TAG, "Sonido equipos balanceados")
    }

    // MÉTODOS COMBINADOS MEJORADOS

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playTimerSequence(timeLeft: Int) {
        when (timeLeft) {
            10 -> playTimeWarning()
            5 -> playSound("countdown_5", 1.0f, 1.0f)
            4 -> playSound("countdown_4", 1.0f, 1.0f)
            3 -> playSound("countdown_3", 1.0f, 1.0f)
            2 -> playSound("countdown_2", 1.0f, 1.0f)
            1 -> playSound("countdown_1", 1.0f, 1.0f)
            0 -> playTimeUp()
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun playAnswerFeedback(isCorrect: Boolean) {
        if (isCorrect) {
            playCorrectAnswer()
        } else {
            playWrongAnswer()
        }
    }

    // MÉTODOS INTERNOS

    private fun playSound(soundKey: String, leftVolume: Float = 1.0f, rightVolume: Float = 1.0f) {
        if (!isEnabled || !soundsLoaded) {
            Log.d(TAG, "Sonidos deshabilitados o no cargados")
            return
        }

        try {
            val soundId = soundIds[soundKey]
            if (soundId != null && soundId > 0) {
                soundPool?.play(soundId, leftVolume, rightVolume, 1, 0, 1.0f)
                Log.d(TAG, "Reproduciendo sonido: $soundKey")
            } else {
                Log.w(TAG, "Sonido no encontrado: $soundKey")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo sonido: $soundKey", e)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(pattern: LongArray, repeat: Int) {
        if (!isEnabled) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, repeat)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrando", e)
        }
    }

    // CONFIGURACIÓN

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Sonidos habilitados: $enabled")
    }

    fun setVolume(volume: Float) {
        // Ajustar volumen global del SoundPool si es necesario
        Log.d(TAG, "Volumen ajustado a: $volume")
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundIds.clear()
            soundsLoaded = false
            Log.d(TAG, "DuelSoundManager liberado")
        } catch (e: Exception) {
            Log.e(TAG, "Error liberando sound manager", e)
        }
    }
}

// Enum para los tipos de sonido (para compatibilidad)
enum class DuelSound {
    CORRECT_ANSWER,
    WRONG_ANSWER,
    TIME_WARNING,
    TIME_UP,
    QUESTION_APPEAR,
    GAME_START,
    PLAYER_JOIN,
    VICTORY,
    TICK,
    BUTTON_CLICK,
    NOTIFICATION,
    PERFECT_SCORE,
    TEAM_BALANCE
}

// Composable helper (sin cambios)
@Composable
fun rememberDuelSoundManager(context: Context): DuelSoundManager {
    return remember { DuelSoundManager.getInstance(context) }
}