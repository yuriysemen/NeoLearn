package com.neolearn.course

import android.content.Context
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class Option(
    val value: String,
    val text: String,
    val dataCorrect: Boolean
)

data class Question(
    val questionId: String,
    val type: String,
    val text: String,
    val options: List<Option>
)

data class UserAnswer(
    val question: Question,
    val selectedAnswers: List<String>,
    val userText: String? = null,
    var maxPoints: Float = 0f,
    var points: Float = 0f
)

data class Variant(
    val variantId: String,
    val questions: List<Question>
)

data class AnswerData(
    val answers: Map<String, List<String>>,
    val variantId: Int
)

class TestDataParsing {

    private fun readAsset(context: Context, path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    fun getTestDataFromString(html: String): List<Variant> {
        val doc: Document = Jsoup.parse(html)
        val variants = mutableListOf<Variant>()

        val variantDivs = doc.select(".quiz-variant")
        for (variant in variantDivs) {
            val variantId = variant.id()
            val questions = mutableListOf<Question>()

            val questionDivs = variant.select(".question")
            for (question in questionDivs) {
                val qId = question.attr("data-question-id")
                val type = question.attr("data-type")

                // Текст питання: перший текстовий вузол
                val textNode = question.ownText().trim()

                // Варіанти відповіді
                val options = mutableListOf<Option>()
                val inputs = question.select("input")
                for (input in inputs) {
                    val value = input.attr("value")
                    val labelText = input.parent()?.ownText()?.trim() ?: ""
                    val dataCorrect = input.attr("data-correct").lowercase() == "true"
                    options.add(Option(value, labelText, dataCorrect))
                }

                questions.add(Question(qId, type, textNode, options))
            }

            variants.add(Variant(variantId, questions))
        }

        return variants
    }
}