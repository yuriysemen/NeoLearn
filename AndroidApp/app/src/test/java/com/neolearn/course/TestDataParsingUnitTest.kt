package com.neolearn.course

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestDataParsingUnitTest {
    private val testHtml = """
<section id="test-section">
  <div id='variant1' class="quiz-variant" style="display:none;">
    <div data-question-id="variant1q1_group" data-type="radio" class='question'>
      1. Яке з чисел є натуральним?<br>
      <label><input type='radio' name='variant1q1' value="2"> –5</label><br>
      <label><input type='radio' name='variant1q1' data-correct='true' value="3"> 12</label><br>
    </div>
    <div data-question-id="variant1q2_group" data-type="checkbox" class='question'>
      2. Вибери всі натуральні:<br>
      <label><input type='checkbox' name='variant1q2' value="1"> 1</label><br>
      <label><input type='checkbox' name='variant1q2' value="4"> 4</label><br>
      <label><input type='checkbox' name='variant1q2' data-correct='true' value="-4"> -4</label><br>
      <label><input type='checkbox' name='variant1q2' data-correct='true' value="-3"> -3</label><br>
    </div>
  </div>
  <div id='variant2' class="quiz-variant" style="display:none;">
    <div data-question-id="variant2q1_group" data-type="radio" class='question'>
      1. Яке з чисел не є натуральним?<br>
      <label><input type='radio' name='variant1q1' data-correct='true' value="2"> –52</label><br>
      <label><input type='radio' name='variant1q1' value="3"> 122</label><br>
    </div>
  </div>
</section>
""".trimIndent()

    @Test
    fun `parseHtml should return one variant`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        assertEquals(2, result.size)
        assertEquals("variant1", result.first().variantId)
        assertEquals("variant2", result.last().variantId)
    }

    @Test
    fun `variant should have two questions`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val questions1 = result.first().questions
        assertEquals(2, questions1.size)

        val questions2 = result.last().questions
        assertEquals(1, questions2.size)
    }

    @Test
    fun `first question should be radio and have text`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val firstQuestion = result.first().questions[0]

        assertEquals("radio", firstQuestion.type)
        assertTrue(firstQuestion.text.startsWith("1. Яке з чисел є натуральним"))
    }

    @Test
    fun `first question should have 2 options`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val options = result.first().questions[0].options

        assertEquals(2, options.size)
        assertEquals("2", options[0].value)
        assertEquals("–5", options[0].text)
    }

    @Test
    fun `checkbox question should have multiple options`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val checkboxQuestion = result.first().questions[1]

        assertEquals("checkbox", checkboxQuestion.type)
        assertEquals(4, checkboxQuestion.options.size)
        assertEquals("1", checkboxQuestion.options[0].value)
        assertEquals("4", checkboxQuestion.options[1].value)
        assertEquals("-4", checkboxQuestion.options[2].value)
        assertEquals("-3", checkboxQuestion.options[3].value)
    }

    @Test
    fun `correct options should be read properly from checkboxes`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val checkboxQuestion = result.first().questions[1]

        assertEquals("checkbox", checkboxQuestion.type)
        assertEquals(4, checkboxQuestion.options.size)
        assertEquals(false, checkboxQuestion.options[0].dataCorrect)
        assertEquals(false, checkboxQuestion.options[1].dataCorrect)
        assertEquals(true, checkboxQuestion.options[2].dataCorrect)
        assertEquals(true, checkboxQuestion.options[3].dataCorrect)
    }


    @Test
    fun `correct options should be read properly from radio`() {
        val result = TestDataParsing().getTestDataFromString(testHtml)
        val checkboxQuestion = result.first().questions[0]

        assertEquals("radio", checkboxQuestion.type)
        assertEquals(2, checkboxQuestion.options.size)
        assertEquals(false, checkboxQuestion.options[0].dataCorrect)
        assertEquals(true, checkboxQuestion.options[1].dataCorrect)
    }
}