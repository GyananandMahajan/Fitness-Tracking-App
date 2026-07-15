package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonLime
import com.example.ui.theme.RealBg
import com.example.ui.theme.RealSurface
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.FitnessViewModel
import com.example.ui.viewmodel.FitnessViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val fitnessViewModel: FitnessViewModel = viewModel(
                    factory = FitnessViewModelFactory(app)
                )

                val prefs by fitnessViewModel.userPreferences.collectAsState()

                if (prefs != null) {
                    if (prefs?.onboardingCompleted == false) {
                        OnboardingScreen(viewModel = fitnessViewModel)
                    } else {
                        MainScreenContainer(viewModel = fitnessViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenContainer(viewModel: FitnessViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = RealSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = "Dashboard",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Workspace") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RealBg,
                        selectedTextColor = NeonLime,
                        indicatorColor = NeonLime,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trends",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Trends") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RealBg,
                        selectedTextColor = NeonLime,
                        indicatorColor = NeonLime,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_tab_trends")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Social",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Community") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RealBg,
                        selectedTextColor = NeonLime,
                        indicatorColor = NeonLime,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray
                    ),
                    modifier = Modifier.testTag("nav_tab_social")
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> TrendsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> SocialScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
