package com.renobile.carrinho.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object TextRecognitionHelper {

    private val STOP_WORDS = setOf(
        "lista de compras",
        "shopping list",
        "listas e notas",
        "listas",
        "adicionar item",
        "adicionar à lista",
        "adicionar à lista de compras",
        "add item",
        "add to shopping list",
        "itens",
        "itens da lista",
        "itens concluídos",
        "concluído",
        "concluídos",
        "completed",
        "comprado",
        "comprados",
        "alexa",
        "amazon",
        "início",
        "conversar",
        "comunicar",
        "dispositivos",
        "mais",
        "home",
        "devices",
        "more",
    )

    private val TIME_REGEX = Regex("""^\d{1,2}:\d{2}(\s*[aApP][mM])?$""")
    private val BATTERY_OR_SIGNAL_REGEX = Regex("""^(\d{1,3}%|4g|5g|lte|wi-?fi)$""", RegexOption.IGNORE_CASE)
    private val PREFIX_CLEAN_REGEX = Regex("""^(?:[•\-\*\+▫▪◻◽◾oO0☑☐✓]|\[[\sxX]*\]|\(\s*\)|\d+[\.\-\)])\s*""")

    fun parseRawTextToShoppingItems(rawText: String): List<String> {
        val lines = rawText.split(Regex("""\r?\n"""))
        val result = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // If we hit "Concluído" or "Itens concluídos" in Alexa, everything after is already completed.
            val lower = trimmed.lowercase()
            if (lower == "concluído" || lower == "concluídos" || lower == "completed" || lower == "itens concluídos") {
                break
            }

            // Skip status bar noise
            if (TIME_REGEX.matches(trimmed) || BATTERY_OR_SIGNAL_REGEX.matches(trimmed)) {
                continue
            }

            // Skip stop words
            if (STOP_WORDS.any { lower == it || lower.startsWith("$it ") }) {
                continue
            }

            // Clean prefixes (bullets, numbers, checkboxes)
            val cleaned = trimmed.replace(PREFIX_CLEAN_REGEX, "").trim()

            // Discard if cleaned line is too short, matches stop words, or was already added
            val cleanedLower = cleaned.lowercase()
            if (cleaned.length >= 2 && STOP_WORDS.none { cleanedLower == it } && seen.add(cleanedLower)) {
                result.add(cleaned)
            }
        }

        return result
    }

    suspend fun extractShoppingListFromImage(context: Context, imageUri: Uri): Result<List<String>> =
        suspendCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val items = parseRawTextToShoppingItems(visionText.text)
                        continuation.resume(Result.success(items))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
}
