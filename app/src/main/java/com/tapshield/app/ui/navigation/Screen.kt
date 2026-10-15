package com.tapshield.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Shield",
        icon = Icons.Default.Security
    )

    data object Contacts : Screen(
        route = "contacts",
        title = "Contacts",
        icon = Icons.Default.Contacts
    )

    data object ActiveSos : Screen(
        route = "active_sos",
        title = "Active SOS",
        icon = Icons.Default.Warning
    )

    data object SafetySetup : Screen(
        route = "setup",
        title = "Setup",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(Home, Contacts, ActiveSos, SafetySetup)
    }
}
