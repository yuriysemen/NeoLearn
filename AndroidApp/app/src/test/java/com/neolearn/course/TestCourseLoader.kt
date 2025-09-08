package com.neolearn.course

import androidx.compose.runtime.Composable
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestCourseLoader {

    @Test
    fun `In short representation simplified case should be translated Radio`() {
        val testHtml = "    <div id=\"quiz-form\" style=\"margin-top: 10px;\">\n" +
                "        <div id='variant1' class=\"quiz-variant\" style=\"display:none;\">\n" +
                "            <div data-question-id=\"variant1q1_group\" data-type=\"radio\" representation=\"short\">\n" +
                "                <q>1. Яке з чисел є натуральним?</q>\n" +
                "                <li>–5</li>\n" +
                "                <li data-correct='true'>12</li>\n" +
                "                <li>0</li>\n" +
                "                <li>3.5</li>\n" +
                "                <li>-10</li>\n" +
                "            </div>\n" +
                "          </div>\n";

        val expectedHtml = "<div id=\"quiz-form\" style=\"margin-top: 10px;\">\n" +
                " <div id=\"variant1\" class=\"quiz-variant\" style=\"display:none;\">\n" +
                "  <div data-question-id=\"variant1q1_group\" data-type=\"radio\" class=\"question\">\n" +
                "   1. Яке з чисел є натуральним?\n   <br>\n" +
                "   <label><input type=\"radio\" name=\"variant1q1\" value=\"–5\"> –5</label>\n   <br>\n" +
                "   <label><input type=\"radio\" name=\"variant1q1\" value=\"12\" data-correct=\"true\"> 12</label>\n   <br>\n" +
                "   <label><input type=\"radio\" name=\"variant1q1\" value=\"0\"> 0</label>\n   <br>\n" +
                "   <label><input type=\"radio\" name=\"variant1q1\" value=\"3.5\"> 3.5</label>\n   <br>\n" +
                "   <label><input type=\"radio\" name=\"variant1q1\" value=\"-10\"> -10</label>\n   <br>\n" +
                "  </div>\n" +
                " </div>\n" +
                "</div>"
        assertEquals(expectedHtml, CourseLoader.prepareQuestions(testHtml))
    }

    @Test
    fun `In short representation simplified case should be translated Checkbox`() {
        val testHtml = "    <div id=\"quiz-form\" style=\"margin-top: 10px;\">\n" +
                "        <div id='variant1' class=\"quiz-variant\" style=\"display:none;\">\n" +
                "            <div data-question-id=\"variant1q1_group\" data-type=\"checkbox\" representation=\"short\">\n" +
                "                <q>1. Яке з чисел є натуральним?</q>\n" +
                "                <li>–5</li>\n" +
                "                <li data-correct='true'>12</li>\n" +
                "                <li>0</li>\n" +
                "                <li data-correct='true'>3</li>\n" +
                "                <li>-10</li>\n" +
                "            </div>\n" +
                "          </div>\n";

        val expectedHtml = "<div id=\"quiz-form\" style=\"margin-top: 10px;\">\n" +
                " <div id=\"variant1\" class=\"quiz-variant\" style=\"display:none;\">\n" +
                "  <div data-question-id=\"variant1q1_group\" data-type=\"checkbox\" class=\"question\">\n" +
                "   1. Яке з чисел є натуральним?\n   <br>\n" +
                "   <label><input type=\"checkbox\" name=\"variant1q1\" value=\"–5\"> –5</label>\n   <br>\n" +
                "   <label><input type=\"checkbox\" name=\"variant1q1\" value=\"12\" data-correct=\"true\"> 12</label>\n   <br>\n" +
                "   <label><input type=\"checkbox\" name=\"variant1q1\" value=\"0\"> 0</label>\n   <br>\n" +
                "   <label><input type=\"checkbox\" name=\"variant1q1\" value=\"3\" data-correct=\"true\"> 3</label>\n   <br>\n" +
                "   <label><input type=\"checkbox\" name=\"variant1q1\" value=\"-10\"> -10</label>\n   <br>\n" +
                "  </div>\n" +
                " </div>\n" +
                "</div>"

        assertEquals(expectedHtml, CourseLoader.prepareQuestions(testHtml))
    }
}