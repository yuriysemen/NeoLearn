package com.neolearn

import CourseLoader
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.WelcomeScreen
import com.neolearn.course.Course
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit
import com.neolearn.course.Lesson


@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onStartClicked = { navController.navigate("courseDetailsScreen") }
            )
        }
        composable("courseDetailsScreen") {
            CourseDetailsScreen(
                "matem 6",
                onModuleClick = {
                    course: Course, module: Module ->
                        navController.navigate("moduleDetailsScreen/${course.id}/${module.id}")
                }
            )
        }
        composable(
            route = "moduleDetailsScreen/{courseId}/{moduleId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("moduleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val moduleId = backStackEntry.arguments?.getString("moduleId")

            if ((courseId != null) and (moduleId != null)) {
                ModuleDetailsScreen(
                    courseId!!,
                    moduleId!!,
                    onUnitClick = {
                        course: Course, module: Module, unit: CourseUnit ->
                        navController.navigate("unitDetailsScreen/${course.id}/${module.id}/${unit.id}")
                    }
                )
            }
        }
        composable(
            route = "unitDetailsScreen/{courseId}/{moduleId}/{unitId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("unitId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            val unitId = backStackEntry.arguments?.getString("unitId")

            if ((courseId != null) and (moduleId != null) and (unitId != null)) {
                UnitDetailsScreen(
                    courseId!!,
                    moduleId!!,
                    unitId!!,
                    onUnitClick = {
                            course: Course, module: Module, unit: CourseUnit, lesson: Lesson ->
                        navController.navigate("lessonDetailsScreen/${course.id}/${module.id}/${unit.id}/${lesson.id}")
                    }
                )
            }
        }
    }
}
