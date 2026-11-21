package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream

sealed interface AiImageUiState {
    data object Idle : AiImageUiState
    data object Loading : AiImageUiState
    data class Success(val localFileUri: String, val bitmap: Bitmap) : AiImageUiState
    data class Error(val message: String) : AiImageUiState
}

sealed interface AiAdviceUiState {
    data object Idle : AiAdviceUiState
    data object Loading : AiAdviceUiState
    data class Success(val adviceText: String) : AiAdviceUiState
    data class Error(val message: String) : AiAdviceUiState
}

class NailAiRepository {

    private val imageModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
            }
        )
    }

    private val stylistModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )
    }

    suspend fun generateAvatarOrNailConcept(
        context: Context,
        userPrompt: String
    ): Result<Pair<String, Bitmap>> {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isEmpty()) {
            return Result.failure(IllegalArgumentException("Введите описание для генерации изображения."))
        }

        return try {
            val fullPrompt = "Stylish high-resolution nail studio avatar or manicure concept art for NailiX app, clean modern editorial lighting, zero watermarks: $cleanPrompt"
            val bitmap = withTimeout(60_000L) {
                val response = imageModel.generateContent(fullPrompt)
                val imagePart = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()
                imagePart?.image
            } ?: return Result.failure(IllegalStateException("Модель Gemini не вернула изображение."))

            val savedUri = withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "ai_avatars")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "nailix_ai_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
                Uri.fromFile(file).toString()
            }
            Result.success(savedUri to bitmap)
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Превышено время ожидания генерации (60 сек). Повторите попытку."))
        } catch (e: QuotaExceededException) {
            Result.failure(Exception("Превышена квота Firebase AI Logic / требуется Blaze-тариф: ${e.localizedMessage}"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI image generation failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Ошибка генерации через Firebase AI Logic."))
        }
    }

    suspend fun generateNailStylistAdvice(
        occasion: String,
        preferredLength: String,
        preferredShape: String,
        preferredPalette: String,
        languageCode: String
    ): Result<String> {
        val langLabel = when (languageCode) {
            "es" -> "Spanish"
            "en" -> "English"
            else -> "Russian"
        }
        val prompt = """
            You are a senior nail stylist at NailiX studio. Respond concisely in $langLabel (3-4 bullet points max).
            Client preferences:
            - Occasion / mood: ${occasion.ifBlank { "Everyday elegance" }}
            - Nail length: $preferredLength
            - Shape: $preferredShape
            - Palette: $preferredPalette
            Recommend: 1) Base & shade combination, 2) Accent nail art technique, 3) Recommended care/service type & estimated salon time.
        """.trimIndent()

        return try {
            val text = withTimeout(45_000L) {
                val response = stylistModel.generateContent(prompt)
                response.text?.trim()
            }
            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("ИИ-стилист вернул пустой ответ."))
            } else {
                Result.success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("ИИ-стилист не ответил за 45 сек."))
        } catch (e: QuotaExceededException) {
            Result.failure(Exception("Превышена квота Firebase AI Logic: ${e.localizedMessage}"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI stylist advice failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Ошибка обращения к Gemini AI."))
        }
    }

    suspend fun analyzeHandOrInspirationPhoto(
        bitmap: Bitmap,
        question: String,
        languageCode: String
    ): Result<String> {
        val langLabel = when (languageCode) {
            "es" -> "Spanish"
            "en" -> "English"
            else -> "Russian"
        }
        val scaledBitmap = scaleBitmapForVision(bitmap, maxSidePx = 1024)
        val userNote = question.trim().ifBlank {
            "Analyze the hand or nail design reference and recommend ideal shape, length, color palette, and salon service."
        }
        val prompt = """
            You are a senior nail stylist at NailiX studio. Respond concisely in $langLabel (4 short bullet points max).
            Examine the attached photo (client's hand or nail art reference) and user note: "$userNote".
            Provide:
            1) Suitable nail shape & length recommendation,
            2) Best matching gel polish shade palette & accent design technique,
            3) Recommended studio service & estimated duration in minutes.
        """.trimIndent()

        return try {
            val text = withTimeout(60_000L) {
                val response = stylistModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
                response.text?.trim()
            }
            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("Gemini Vision не вернул описание."))
            } else {
                Result.success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Анализ фото превысил лимит 60 сек. Попробуйте снова."))
        } catch (e: QuotaExceededException) {
            Result.failure(Exception("Превышена квота Firebase AI Logic: ${e.localizedMessage}"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI vision analysis failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Ошибка анализа фото через Gemini Vision."))
        }
    }

    private fun scaleBitmapForVision(source: Bitmap, maxSidePx: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= maxSidePx || longest <= 0) return source
        val scale = maxSidePx.toFloat() / longest.toFloat()
        val targetW = (source.width * scale).toInt().coerceAtLeast(1)
        val targetH = (source.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private companion object {
        const val TAG = "NailAiRepository"
    }
}
