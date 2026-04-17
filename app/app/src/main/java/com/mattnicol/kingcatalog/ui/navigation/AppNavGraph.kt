package com.mattnicol.kingcatalog.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mattnicol.kingcatalog.ui.screens.books.BooksScreen
import com.mattnicol.kingcatalog.ui.screens.detail.BookDetailScreen
import com.mattnicol.kingcatalog.ui.screens.home.HomeScreen
import com.mattnicol.kingcatalog.ui.screens.library.LibraryScreen
import com.mattnicol.kingcatalog.ui.screens.other.OtherAuthorsScreen
import com.mattnicol.kingcatalog.ui.screens.series.SeriesGroupDetailScreen
import com.mattnicol.kingcatalog.ui.screens.series.SeriesScreen

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector) {
    data object Home : NavRoute("home", "Home", Icons.Filled.Home)
    data object Books : NavRoute("books", "Books", Icons.Filled.MenuBook)
    data object Library : NavRoute("library", "My Library", Icons.Filled.LibraryBooks)
    data object Series : NavRoute("series", "Series", Icons.Filled.Bookmarks)
    data object OtherAuthors : NavRoute("other_authors", "Other Authors", Icons.Filled.People)
}

private val bottomNavItems = listOf(
    NavRoute.Home,
    NavRoute.Books,
    NavRoute.Library,
    NavRoute.Series,
    NavRoute.OtherAuthors,
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
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
            composable(NavRoute.Home.route) {
                HomeScreen(onBookClick = { id -> navController.navigate("book_detail/$id") })
            }
            composable(NavRoute.Books.route) {
                BooksScreen(onBookClick = { id -> navController.navigate("book_detail/$id") })
            }
            composable(NavRoute.Library.route) {
                LibraryScreen(onBookClick = { id -> navController.navigate("book_detail/$id") })
            }
            composable(NavRoute.Series.route) {
                SeriesScreen(
                    onGroupClick = { groupName ->
                        navController.navigate("series_detail/${encodeNavArg(groupName)}")
                    },
                )
            }
            composable(
                route = "series_detail/{groupName}",
                arguments = listOf(navArgument("groupName") { type = NavType.StringType }),
            ) { backStackEntry ->
                val groupName = backStackEntry.arguments!!.getString("groupName") ?: ""
                SeriesGroupDetailScreen(
                    groupName = decodeNavArg(groupName),
                    onBack = { navController.popBackStack() },
                    onBookClick = { id -> navController.navigate("book_detail/$id") },
                )
            }
            composable(NavRoute.OtherAuthors.route) {
                OtherAuthorsScreen(onBookClick = { id -> navController.navigate("book_detail/$id") })
            }
            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.IntType }),
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments!!.getInt("bookId")
                BookDetailScreen(bookId = bookId, onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Simple URL-encode for nav arguments that may contain slashes or spaces. */
private fun encodeNavArg(value: String): String =
    java.net.URLEncoder.encode(value, "UTF-8")

private fun decodeNavArg(value: String): String =
    java.net.URLDecoder.decode(value, "UTF-8")
