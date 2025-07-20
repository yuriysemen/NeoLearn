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
import com.neolearn.course.Module
import com.neolearn.course.CourseUnit


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
                    module: Module ->
                        navController.navigate("moduleDetailsScreen/${module.id}")
                }
            )
        }
        composable(
            route = "moduleDetailsScreen/{moduleId}",
            arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")

            val course = CourseLoader.loadCourse(LocalContext.current, "materials/matem 6")
            val module = CourseLoader.loadModules(LocalContext.current, "materials/matem 6")
                .find { predicate -> predicate.id == moduleId }!!
            val units = CourseLoader.loadUnits(LocalContext.current, module.locatedAt!!)
            ModuleDetailsScreen(
                course,
                module,
                units,
                onUnitClick = {
                    unit: CourseUnit -> navController.navigate("unitDetailsScreen/${unit.id}")
                }
            )
        }
        composable("second") {
            SecondScreen(
//                onBack = { navController.popBackStack() }
            )
        }
    }
}
