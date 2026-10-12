package com.ezora.android.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ezora.android.data.remote.UserDto
import com.ezora.android.ui.components.ComingSoon

private enum class Tab(val label: String, val selected: ImageVector, val unselected: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Plan("Plan", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    Historial("Historial", Icons.Filled.History, Icons.Outlined.History),
    Stock("Stock", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    Mas("Más", Icons.Filled.Menu, Icons.Outlined.Menu),
}

/** Barra inferior de 5 pestañas, igual que la navegación de la app web del visitador. */
@Composable
fun MainScaffold(user: UserDto, onLogout: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val current = Tab.entries[tab]

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEachIndexed { index, t ->
                    NavigationBarItem(
                        selected = index == tab,
                        onClick = { tab = index },
                        icon = { Icon(if (index == tab) t.selected else t.unselected, contentDescription = t.label) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (current) {
            Tab.Inicio -> HomeScreen(user = user, modifier = modifier)
            Tab.Plan -> ComingSoon("Plan del ciclo", "Planificación semanal y diaria (fase 2).", modifier)
            Tab.Historial -> ComingSoon("Historial", "Tus visitas registradas (fase 2).", modifier)
            Tab.Stock -> ComingSoon("Stock y reportes", "Muestras, facturas y reportes (fase 3).", modifier)
            Tab.Mas -> MoreScreen(user = user, onLogout = onLogout, modifier = modifier)
        }
    }
}
