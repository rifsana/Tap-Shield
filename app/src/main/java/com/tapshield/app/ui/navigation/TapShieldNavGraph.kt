package com.tapshield.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tapshield.app.domain.state.SosState
import com.tapshield.app.ui.screens.ActiveSosScreen
import com.tapshield.app.ui.screens.ContactsScreen
import com.tapshield.app.ui.screens.HomeScreen
import com.tapshield.app.ui.screens.SafetySetupScreen
import com.tapshield.app.ui.theme.EmergencyRed
import com.tapshield.app.ui.theme.ShieldCyan
import com.tapshield.app.ui.theme.SurfaceElevatedDark
import com.tapshield.app.ui.viewmodel.ActiveSosViewModel
import com.tapshield.app.ui.viewmodel.ContactsViewModel
import com.tapshield.app.ui.viewmodel.HomeViewModel
import com.tapshield.app.ui.viewmodel.SafetySetupViewModel
import com.tapshield.app.ui.viewmodel.TapShieldViewModelFactory

@Composable
fun TapShieldNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModelFactory: TapShieldViewModelFactory = TapShieldViewModelFactory()
) {
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val contactsViewModel: ContactsViewModel = viewModel(factory = viewModelFactory)
    val activeSosViewModel: ActiveSosViewModel = viewModel(factory = viewModelFactory)
    val safetySetupViewModel: SafetySetupViewModel = viewModel(factory = viewModelFactory)

    val sosState by homeViewModel.sosState.collectAsState()
    val isEmergencyActive = sosState is SosState.Active || sosState is SosState.Countdown

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    androidx.compose.runtime.LaunchedEffect(sosState) {
        if (sosState is SosState.Active && currentRoute != Screen.ActiveSos.route) {
            navController.navigate(Screen.ActiveSos.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceElevatedDark,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    val isSosTab = screen == Screen.ActiveSos

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (isSosTab && isEmergencyActive) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = EmergencyRed)
                                    }
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (selected) ShieldCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isSosTab && isEmergencyActive) EmergencyRed else ShieldCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = if (isSosTab && isEmergencyActive) EmergencyRed.copy(alpha = 0.2f) else ShieldCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToActiveSos = {
                        navController.navigate(Screen.ActiveSos.route)
                    },
                    onNavigateToContacts = {
                        navController.navigate(Screen.Contacts.route)
                    },
                    onNavigateToSetup = {
                        navController.navigate(Screen.SafetySetup.route)
                    }
                )
            }

            composable(Screen.Contacts.route) {
                ContactsScreen(
                    viewModel = contactsViewModel
                )
            }

            composable(Screen.ActiveSos.route) {
                ActiveSosScreen(
                    viewModel = activeSosViewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            composable(Screen.SafetySetup.route) {
                SafetySetupScreen(
                    viewModel = safetySetupViewModel
                )
            }
        }
    }
}
