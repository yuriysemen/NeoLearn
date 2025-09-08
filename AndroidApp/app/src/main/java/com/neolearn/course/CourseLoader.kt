import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.neolearn.copyAssetToCache
import com.neolearn.course.Course
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit
import com.neolearn.course.Lesson
import com.neolearn.toHex
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object CourseLoader {
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    fun loadCourse(context: Context, courseFilePath: String): Course {
        val input = context.assets.open("materials/$courseFilePath/course.yaml")
        val result = mapper.readValue(input, Course::class.java)
        result.locatedAt = "$courseFilePath/course.yaml"
        return result
    }

    fun loadModules(context: Context, modulesPath: String): List<Module> {
        val assetManager = context.assets
        val filesList = assetManager.list("materials/$modulesPath") ?: return emptyList()

        val modules = filesList
            .filter { folder ->
                val fullPath = "materials/$modulesPath/$folder/module.yaml"
                try {
                    assetManager.open(fullPath).close() // Try to open and close the file
                    true // File exists
                } catch (e: Exception) {
                    Log.d(this::class.java.simpleName, "Error opening module.yaml file at folder: $folder")
                    false // File not found
                }
            }
            .mapNotNull { folder ->
                assetManager.open("materials/$modulesPath/$folder/module.yaml").use { input ->
                    val output = mapper.readValue(input, Module::class.java)
                    output.locatedAt = "$modulesPath/$folder"
                    output
                }
        }

        return modules
    }

    fun loadModule(context: Context, coursePath: String, modulePath: String): Module {
        val assetManager = context.assets

        return assetManager.open("materials/$coursePath/$modulePath/module.yaml").use { input ->
            val output = mapper.readValue(input, Module::class.java)
            output.locatedAt = "$coursePath/$modulePath"
            output
        }
    }

    fun loadUnits(context: Context, coursePath: String, unitsPath: String): List<CourseUnit> {
        val assetManager = context.assets
        val filesList = assetManager.list("materials/$coursePath/$unitsPath") ?: return emptyList()

        return filesList
            .filter { folder ->
                val fullPath = "materials/$coursePath/$unitsPath/$folder/unit.yaml"
                try {
                    assetManager.open(fullPath).close() // Try to open and close the file
                    true // File exists
                } catch (e: Exception) {
                    false // File not found
                }
            }
            .mapNotNull { folder ->
                assetManager.open("materials/$coursePath/$unitsPath/$folder/unit.yaml").use { input ->
                    mapper.readValue(input, CourseUnit::class.java)
                }
        }
    }

    fun loadUnit(context: Context, coursePath: String, modulePath: String, unitPath: String): CourseUnit {
        val assetManager = context.assets

        return assetManager.open("materials/$coursePath/$modulePath/$unitPath/unit.yaml").use { input ->
            val output = mapper.readValue(input, CourseUnit::class.java)
            output.locatedAt = "$coursePath/$modulePath/$unitPath"
            output
        }
    }

    fun loadLessons(context: Context, coursePath: String, modulePath: String, unit: String): List<Lesson> {
        val assetManager = context.assets
        val filesList = assetManager.list("materials/$coursePath/$modulePath/$unit") ?: return emptyList()

        return filesList
            .filter { folder ->
                val fullPath = "materials/$coursePath/$modulePath/$unit/$folder/lesson.yaml"
                try {
                    assetManager.open(fullPath).close() // Try to open and close the file
                    true // File exists
                } catch (e: Exception) {
                    false // File not found
                }
            }
            .mapNotNull { folder ->
                assetManager.open("materials/$coursePath/$modulePath/$unit/$folder/lesson.yaml").use { input ->
                    mapper.readValue(input, Lesson::class.java)
                }
            }
    }

    fun loadLesson(
        context: Context,
        coursePath: String,
        modulePath: String,
        unitPath: String,
        lessonPath: String
    ): Lesson {
        val assetManager = context.assets

        return assetManager.open("materials/$coursePath/$modulePath/$unitPath/$lessonPath/lesson.yaml").use { input ->
            val output = mapper.readValue(input, Lesson::class.java)
            output.locatedAt = "$coursePath/$modulePath/$unitPath/$lessonPath"
            output
        }
    }

    private fun readAsset(context: Context, path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    @Composable
    fun prepareActivity(context: Context, activityPath: String): Pair<String, String> {
        var header = readAsset(context, "header.html")
        var footer = readAsset(context, "footer.html")
        var body = readAsset(context, activityPath)

        copyAssetToCache(context, "katex/katex.min.css", "katex.min.css")
        copyAssetToCache(context, "logic.js", "logic.js")
        copyAssetToCache(context, "katex/katex.min.js", "katex.min.js")
        copyAssetToCache(context, "katex/auto-render.min.js", "auto-render.min.js")

        body = prepareQuestions(body)

        header = prepareColorSchema(header)
        footer = prepareColorSchema(footer)
        body = prepareColorSchema(body)

        return Pair(header + body + footer, context.cacheDir.toURI().toString())
    }

    fun prepareQuestions(body: String): String {
        val doc = Jsoup.parse(body)

        val shortDivs = doc.select("div[representation=short]")

        for (div in shortDivs) {
            val questionId = div.attr("data-question-id")
            val questionText = div.selectFirst("q")?.text() ?: "Запитання"
            val answers = div.select("li")

            val newDiv = Element("div")
                .attr("data-question-id", questionId)
                .attr("data-type", div.attr("data-type"))
                .addClass("question")
            val type = div.attr("data-type")


            newDiv.appendText(questionText)
            newDiv.appendElement("br")

            val name = questionId.removeSuffix("_group")

            // генеруємо варіанти відповідей
            for (answer in answers) {
                val value = answer.text()
                val label = Element("label")
                val input = Element("input")
                    .attr("type", type)
                    .attr("name", name)
                    .attr("value", value)

                // переносимо data-correct якщо є
                if (answer.hasAttr("data-correct")) {
                    input.attr("data-correct", "true")
                }

                label.appendChild(input)
                label.appendText(" $value")
                newDiv.appendChild(label)
                newDiv.appendElement("br")
            }

            // замінюємо оригінальний div новим
            div.replaceWith(newDiv)
        }

        return doc.body().html()
    }

    @Composable
    private fun prepareColorSchema(content: String): String =
        content.replace("{{background}}", MaterialTheme.colorScheme.primaryContainer.toHex())
            .replace("{{text}}", MaterialTheme.colorScheme.onPrimaryContainer.toHex())
            .replace("{{math}}", MaterialTheme.colorScheme.secondary.toHex())
            .replace("{{infoBackground}}", "rgba(255,255,255,0.1)")
            .replace("{{infoBorder}}", "#fff")
            .replace("{{info}}", "#03a9f4")
            .replace("{{warning}}", "#ff9800")
            .replace("{{task}}", "#4caf50")
            .replace("{{question}}", "#9575cd")
            .replace("{{reference}}", "#26c6da")
            .replace("{{hint}}", "#aed581")
            .replace("{{term}}", "#f06292")

}
