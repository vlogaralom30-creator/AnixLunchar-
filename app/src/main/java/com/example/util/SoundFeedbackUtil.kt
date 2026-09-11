package com.example.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundFeedbackUtil {

    /**
     * Plays the standard system UI click sound effect.
     */
    fun playClickSound(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.9f)
        } catch (e: Exception) {
            // Audio fallback safety
        }
    }

    /**
     * Plays tactile mechanical click vibration.
     */
    fun playClickHaptic(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20L)
                }
            }
        } catch (e: Exception) {
            // Vibrator fallback safety
        }
    }

    /**
     * Plays click feedback based on individual sound and haptic preferences.
     */
    fun playClickFeedback(context: Context, enableSound: Boolean, enableHaptic: Boolean) {
        if (enableSound) {
            playClickSound(context)
        }
        if (enableHaptic) {
            playClickHaptic(context)
        }
    }

    /**
     * Legacy helper for click sound & haptic.
     */
    fun playClickSoundAndHaptic(context: Context) {
        playClickSound(context)
        playClickHaptic(context)
    }

    /**
     * Subtle tick sound when scrolling through items in the launcher.
     */
    fun playScrollTickSound(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.35f)
        } catch (e: Exception) {
            // Audio fallback safety
        }
    }

    /**
     * Subtle tick vibration when scrolling through items in the launcher.
     */
    fun playScrollTickHaptic(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(10L)
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    /**
     * Plays scroll tick feedback based on individual sound and haptic preferences.
     */
    fun playScrollTickFeedback(context: Context, enableSound: Boolean, enableHaptic: Boolean) {
        if (enableSound) {
            playScrollTickSound(context)
        }
        if (enableHaptic) {
            playScrollTickHaptic(context)
        }
    }

    /**
     * Legacy scroll tick helper.
     */
    fun playScrollTick(context: Context) {
        playScrollTickSound(context)
        playScrollTickHaptic(context)
    }
}

