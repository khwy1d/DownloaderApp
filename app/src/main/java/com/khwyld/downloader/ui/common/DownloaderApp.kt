package com.khwyld.downloader.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.khwyld.downloader.di.AppContainer
import com.khwyld.downloader.ui.active.ActiveDownloadsScreen
import com.khwyld.downloader.ui.active.ActiveDownloadsViewModel
import com.khwyld.downloader.ui.browser.BrowserScreen
import com.khwyld.downloader.ui.browser.BrowserViewModel
import com.khwyld.downloader.ui.library.LibraryScreen
import com.khwyld.downloader.ui.library.LibraryViewModel

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Active : Tab("active", "التنزيلات", Icons.Filled.CloudDownload)
    object Browser : Tab("browser", "المتصفح", Icons.Filled.Public)
    object Library : Tab("library", "المكتبة", Icons.Filled.Folder)
}

private val tabs = listOf(Tab.Active, Tab.Browser, Tab.Library)

@Composable
fun DownloaderApp(container: AppContainer, pendingSharedUrl: String?) {
    MaterialTheme {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = backStackEntry?.destination

                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (pendingSharedUrl != null) Tab.Active.route else Tab.Active.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Tab.Active.route) {
                    val vm = remember(container) {
                        ActiveDownloadsViewModel(
                            container.observeActiveDownloadsUseCase,
                            container.refreshActiveDownloadsUseCase,
                            container.cancelDownloadUseCase,
                            container.startDownloadUseCase
                        )
                    }
                    ActiveDownloadsScreen(viewModel = vm, prefillUrl = pendingSharedUrl)
                }
                composable(Tab.Browser.route) {
                    val vm = remember(container) {
                        BrowserViewModel(container.addHistoryUseCase, container.startDownloadUseCase)
                    }
                    BrowserScreen(viewModel = vm)
                }
                composable(Tab.Library.route) {
                    val vm = remember(container) {
                        LibraryViewModel(container.observeLibraryUseCase, container.deleteFromLibraryUseCase)
                    }
                    LibraryScreen(viewModel = vm)
                }
            }
        }
    }
}
