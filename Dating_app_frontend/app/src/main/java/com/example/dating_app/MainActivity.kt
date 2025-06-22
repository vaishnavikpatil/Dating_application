package com.example.dating_app


import BottomNavigationScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dating_app.data.UserPreferences
import com.example.dating_app.ui.login.LoginScreen
import com.example.dating_app.ui.theme.Dating_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Dating_appTheme {
                val navController = rememberNavController()
                val userPrefs = UserPreferences(this)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (userPrefs.isLoggedIn()) "main" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(navController)
                        }
                        composable("main") {
                            BottomNavigationScreen(navController)
                        }
                        // add other composable routes here ...
                    }
                }
            }
        }
    }
}
