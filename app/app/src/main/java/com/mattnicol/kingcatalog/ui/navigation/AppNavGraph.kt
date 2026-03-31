package com.mattnicol.kingcatalog.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mattnicol.kingcatalog.ui.screens.books.BooksScreen
import com.mattnicol.kingcatalog.ui.screens.home.HomeScreen
import com.mattnicol.kingcatalog.ui.screens.library.LibraryScreen
import com.mattnicol.kingcatalog.ui.screens.other.OtherAuthorsScreen

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector) {
    data object Home : NavRoute("home", "Home", Icons.Filled.Home)
    data object Books : NavRoute("books", "Books", Icons.Filled.MenuBook)
    data object Library : NavRoute("library", "Library", Icons.Filled.LibraryBooks)
    data object OtherAuthors : NavRoute("other_authors", "Other Authors", Icons.Filled.People)
}

private val bottomNavItems = listOf(
    NavRoute.Home,
    NavRoute.Books,
    NavRoute.Library,
    NavRoute.OtherAuthors,
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(NavRoute.Home.route) { HomeScreen() }
            composable(NavRoute.Books.route) { BooksScreen() }
            composable(NavRoute.Library.route) { LibraryScreen() }
            composable(NavRoute.OtherAuthors.route) { OtherAuthorsScreen() }
        }
    }
}
