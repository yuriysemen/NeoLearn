import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.neolearn.course.Course
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit

object CourseLoader {
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    fun loadCourse(context: Context, courseFilePath: String): Course {
        val input = context.assets.open("$courseFilePath/course.yaml")
        val result = mapper.readValue(input, Course::class.java)
        result.locatedAt = "$courseFilePath/course.yaml"
        return result
    }

    fun loadModules(context: Context, modulesPath: String): List<Module> {
        val assetManager = context.assets
        val filesList = assetManager.list(modulesPath) ?: return emptyList()

        val modules = filesList
            .filter { folder ->
                val fullPath = "$modulesPath/$folder/module.yaml"
                try {
                    assetManager.open(fullPath).close() // Try to open and close the file
                    true // File exists
                } catch (e: Exception) {
                    false // File not found
                }
            }
            .mapNotNull { folder ->
                assetManager.open("$modulesPath/$folder/module.yaml").use { input ->
                    val output = mapper.readValue(input, Module::class.java)
                    output.locatedAt = "$modulesPath/$folder"
                    output
                }
        }

        return modules
    }

    fun loadUnits(context: Context, unitsPath: String): List<CourseUnit> {
        val assetManager = context.assets
        val filesList = assetManager.list(unitsPath) ?: return emptyList()

        return filesList
            .filter { folder ->
                val fullPath = "$unitsPath/$folder/unit.yaml"
                try {
                    assetManager.open(fullPath).close() // Try to open and close the file
                    true // File exists
                } catch (e: Exception) {
                    false // File not found
                }
            }
            .mapNotNull { folder ->
                assetManager.open("$unitsPath/$folder/unit.yaml").use { input ->
                    mapper.readValue(input, CourseUnit::class.java)
                }
        }
    }
//    fun loadLessons(context: Context, unit: Unit): List<Lesson> {
//        return unit.lessons.map { lessonFile ->
//            val stream = context.assets.open("lessons/$lessonFile")
//            mapper.readValue(stream, Lesson::class.java)
//        }
//    }
}