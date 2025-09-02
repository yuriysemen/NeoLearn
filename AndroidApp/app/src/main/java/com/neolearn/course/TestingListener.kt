package com.neolearn.course

import android.util.Log
import android.webkit.JavascriptInterface
import com.google.gson.Gson

class TestingListener {

    val gson = Gson()
    var testData: List<Variant> = listOf()
    var testVariant: Variant? = null

    @JavascriptInterface
    fun testVariantWasChoose(chosenVariant: String): String {
        Log.i(this.javaClass.name, "Chosen variant: $chosenVariant")
        for (variant in testData) {
            if (variant.variantId == chosenVariant) {
                testVariant = variant

                for (questions in variant.questions) {
                    questions.achievedPoints = 0
                }

                return "Ok"
            }
        }
        throw IllegalArgumentException("Variant could not be found!")
    }

    @JavascriptInterface
    fun sendMaterialTestData(message: String): String {
        if (testVariant == null) {
            Log.i(this.javaClass.name, "Expected to choose variant first.")
            return "Expected to choose variant first."
        }

        val response: String = try {
            val answer = gson.fromJson(message, AnswerData::class.java)
            if (answer.variantId != testVariant?.variantId) {
                Log.e(this.javaClass.name, "Unexpected index of variant: " + answer.variantId + ". Should be " + testVariant?.variantId)
                return "Unexpected index of variant: " + answer.variantId + ". Should be " + testVariant?.variantId
            }

            var nextTaskToBeChecked = Integer.parseInt(answer.questionId.substring(answer.variantId.length + 1).substringBefore("_")) - 1
            if (testVariant?.questions[nextTaskToBeChecked]?.questionId != answer.questionId) {
                Log.e(this.javaClass.name, "Unexpected question id")
                return "Unexpected question id"
            }

            var points = 0
            if (testVariant?.questions[nextTaskToBeChecked]?.type == "radio") {
                for (option in testVariant?.questions[nextTaskToBeChecked]?.options!!) {
                    if (option.value == answer.answer[0] && option.dataCorrect) {
                        points = testVariant?.questions[nextTaskToBeChecked]?.points!!
                        testVariant?.questions[nextTaskToBeChecked]?.achievedPoints = points
                        break
                    }
                }
            }
            else if (testVariant?.questions[nextTaskToBeChecked]?.type == "checkbox") {
                var correctOptions = 0
                for (option in testVariant?.questions[nextTaskToBeChecked]?.options!!) {
                    if (option.dataCorrect) {
                        correctOptions++
                    }
                }

                if (correctOptions == answer.answer.size) {
                    var count = 0;
                    for (userAnswer in answer.answer) {
                        for (option in testVariant?.questions[nextTaskToBeChecked]?.options!!) {
                            if (option.value == userAnswer && option.dataCorrect) {
                                count++
                                break
                            }
                        }
                    }
                    if (count == correctOptions) {
                        points = testVariant?.questions[nextTaskToBeChecked]?.points!!
                        testVariant?.questions[nextTaskToBeChecked]?.achievedPoints = points
                    }
                }
            }
            else {
                return "Unexpected type of the question"
            }

            """{
              |   "userAnswer": ${points}
              |}""".trimMargin()
        } catch (exc: Exception) {
            """{
              |   "error": "${exc.message}"
              |}""".trimMargin()
        }

        return response
    }

    @JavascriptInterface
    fun getTestResult(): String {
        Log.i(this.javaClass.name, "Getting test results.")

        var collectedPoints = 0
        var totalPoints = 0
        for (questions in testVariant?.questions!!) {
            collectedPoints += questions.achievedPoints
            totalPoints += questions.points
        }

        return """{
              |   "variant": "${testVariant?.variantId}",
              |   "collectedPoints": ${collectedPoints},
              |   "totalPoints": ${totalPoints}
              |}""".trimMargin()
    }
}
