

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.dating_app.data.UserPreferences
import com.example.dating_app.ui.chat.ChatScreen
import com.example.dating_app.ui.match.MatchScreen
import com.example.dating_app.ui.profile.ProfileScreen
import com.example.dating_app.ui.swipe.SwipeScreen


sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Swipe : BottomNavItem("swipe", "Swipe", Icons.Default.Email)
    object Match : BottomNavItem("match", "Match", Icons.Default.Favorite)
    object Chat : BottomNavItem("chat", "Chat", Icons.Default.Person)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userPrefs = UserPreferences(context)
    val currentUserId = userPrefs.getUserId()
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Swipe,
        BottomNavItem.Match,
        BottomNavItem.Chat,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController, items = items)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Swipe.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Swipe.route) { SwipeScreen() }
            composable(BottomNavItem.Match.route) { MatchScreen() }
            composable(BottomNavItem.Chat.route) {
                if (currentUserId != null) {
                    ChatScreen(userId = currentUserId )
                }
            }
            composable(BottomNavItem.Profile.route) { ProfileScreen {  } }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController, items: List<BottomNavItem>) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


