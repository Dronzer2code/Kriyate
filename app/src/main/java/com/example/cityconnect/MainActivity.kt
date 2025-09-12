package com.example.cityconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cityconnect.ui.theme.CityConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest


// Government color scheme for professional appearance
object GovColors {
    val NavyBlue = Color(0xFF1B365D)
    val Gold = Color(0xFFFFD700)
    val LightGray = Color(0xFFF5F5F5)
    val DarkGray = Color(0xFF4A4A4A)
    val White = Color.White
    val AccentBlue = Color(0xFF3F7CAC)
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFED6C02)
    val Error = Color(0xFFD32F2F)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CityConnectTheme {
                CityConnectApp()
            }
        }
    }
}

@Composable
fun CityConnectApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("my_reports") { MyReportsScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("new_report") { NewReportScreen(navController) }
            composable("report_submitted") { ReportSubmittedScreen(navController) }
            composable("edit_profile") { EditProfileScreen(navController) }
            composable(
                "report_details/{reportId}",
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) { backStackEntry ->
                ReportDetailsScreen(
                    navController = navController,
                    reportId = backStackEntry.arguments?.getString("reportId")
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.MyReports,
        NavigationItem.Profile
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = GovColors.White)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GovColors.NavyBlue,
                        selectedTextColor = GovColors.NavyBlue,
                        unselectedIconColor = GovColors.DarkGray,
                        unselectedTextColor = GovColors.DarkGray,
                        indicatorColor = GovColors.Gold.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    data object Home : NavigationItem("home", Icons.Default.Home, "Home")
    data object MyReports : NavigationItem("my_reports", Icons.AutoMirrored.Filled.List, "My Reports")
    data object Profile : NavigationItem("profile", Icons.Default.Person, "Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Citizen"
    val displayInitial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "CityConnect",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GovColors.White,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                "Government Portal",
                                fontSize = 12.sp,
                                color = GovColors.Gold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Handle settings click */ }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = GovColors.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GovColors.LightGray)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(GovColors.Gold, GovColors.NavyBlue)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayInitial,
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Welcome, $displayName",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = GovColors.NavyBlue
                        )
                        Text(
                            "Citizen ID: CZ-2025-001",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GovColors.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GovColors.NavyBlue.copy(alpha = 0.8f),
                                        GovColors.AccentBlue.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(0.dp)
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = GovColors.Gold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Report Public Issue",
                                color = GovColors.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Help us maintain our city infrastructure by reporting issues directly to the appropriate department.",
                            color = GovColors.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("new_report") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GovColors.Gold,
                                contentColor = GovColors.NavyBlue
                            ),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(0.dp))
                                .height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Submit New Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "My Reports Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GovColors.NavyBlue
            )
            Spacer(modifier = Modifier.height(16.dp))

            ReportSummaryItem(
                icon = Icons.Default.DateRange,
                title = "Open Reports",
                subtitle = "3 reports pending review",
                count = "3",
                color = GovColors.Warning
            ) {
                navController.navigate("my_reports")
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReportSummaryItem(
                icon = Icons.Default.Refresh,
                title = "Recently Updated",
                subtitle = "2 reports with new updates",
                count = "2",
                color = GovColors.AccentBlue
            ) {
                navController.navigate("my_reports")
            }
        }
    }
}

@Composable
fun ReportSummaryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = GovColors.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = GovColors.NavyBlue
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GovColors.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    count,
                    color = GovColors.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GovColors.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(navController: NavController) {
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Submit New Report",
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovColors.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GovColors.LightGray)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Add Supporting Media",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovColors.NavyBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    GovColors.LightGray,
                                    RoundedCornerShape(0.dp)
                                )
                                .border(
                                    2.dp,
                                    GovColors.AccentBlue.copy(alpha = 0.3f),
                                    RoundedCornerShape(0.dp)
                                )
                                .clickable { /* TODO: Handle image picking */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Upload",
                                    tint = GovColors.AccentBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Upload Photos or Videos",
                                    color = GovColors.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Max 10MB per file",
                                    color = GovColors.DarkGray.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Location Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovColors.NavyBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Address or Cross-streets") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GovColors.AccentBlue,
                                focusedLabelColor = GovColors.AccentBlue
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(0.dp))
                                .background(GovColors.LightGray)
                        ) {
                            // This would ideally be a real map component
                            Image(
                                painter = painterResource(id = R.drawable.map),
                                contentDescription = "Map Location",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Issue Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovColors.NavyBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GovColors.AccentBlue,
                                focusedLabelColor = GovColors.AccentBlue
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GovColors.AccentBlue,
                                focusedLabelColor = GovColors.AccentBlue
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("report_submitted") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(6.dp, RoundedCornerShape(0.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GovColors.Success,
                        contentColor = GovColors.White
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Submit Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSubmittedScreen(navController: NavController) {
    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Report Submitted",
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GovColors.LightGray)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                GovColors.Success.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GovColors.Success,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Report Submitted Successfully",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GovColors.NavyBlue,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your report has been received and assigned tracking ID #RPT-2025-001. You will receive updates on its progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovColors.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GovColors.NavyBlue,
                            contentColor = GovColors.White
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            "Return to Home",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class Report(
    val title: String,
    val id: String,
    val status: String,
    val imageResId: Int,
    val location: String,
    val category: String,
    val description: String
)

val dummyReports = listOf(
    Report(
        title = "Pothole on Benras Road, Howrah",
        id = "20240728-001",
        status = "Resolved",
        imageResId = R.drawable.pothole,
        location = "Benras Road, Howrah",
        category = "Road Issue",
        description = "There is a deep pothole on Benras Road causing inconvenience to commuters."
    ),
    Report(
        title = "Streetlight Outage, Dhankal",
        id = "20240725-003",
        status = "In Progress",
        imageResId = R.drawable.streetlight,
        location = "Dhankal",
        category = "Electrical Issue",
        description = "The main streetlight in the Dhankal area has been out for three days."
    ),
    Report(
        title = "Graffiti on Public Building, Ahiritola",
        id = "20240724-002",
        status = "Acknowledged",
        imageResId = R.drawable.graffiti,
        location = "Ahiritola",
        category = "Vandalism",
        description = "New graffiti has appeared on the wall of the public library in Ahiritola."
    ),
    Report(
        title = "Illegal Dumping, Bali, Howrah",
        id = "20240723-001",
        status = "Submitted",
        imageResId = R.drawable.dumping,
        location = "Bali, Howrah",
        category = "Waste Management",
        description = "Construction debris and household waste have been illegally dumped near the canal."
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "My Reports",
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GovColors.LightGray)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyReports) { report ->
                ReportItem(report) {
                    navController.navigate("report_details/${report.id}")
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = GovColors.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovColors.NavyBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        report.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovColors.DarkGray
                    )
                }
                StatusChip(report.status)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Submitted" -> GovColors.Warning.copy(alpha = 0.1f) to GovColors.Warning
        "In Progress" -> GovColors.AccentBlue.copy(alpha = 0.1f) to GovColors.AccentBlue
        "Acknowledged" -> GovColors.AccentBlue.copy(alpha = 0.1f) to GovColors.AccentBlue
        "Resolved" -> GovColors.Success.copy(alpha = 0.1f) to GovColors.Success
        else -> GovColors.LightGray to GovColors.DarkGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(0.dp)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(navController: NavController, reportId: String?) {
    val report = dummyReports.find { it.id == reportId }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Report Details",
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovColors.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        if (report != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GovColors.LightGray)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = report.imageResId),
                    contentDescription = "Report Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = GovColors.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Report #${report.id}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovColors.NavyBlue
                                )
                                StatusChip(report.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                report.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = GovColors.NavyBlue
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = GovColors.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GovColors.NavyBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(Icons.Default.LocationOn, "Location", report.location)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(Icons.AutoMirrored.Filled.List, "Category", report.category)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow(Icons.Default.Info, "Description", report.description)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Report not found.")
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GovColors.AccentBlue,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GovColors.NavyBlue
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = GovColors.DarkGray
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    // This function will reload the user from Firebase to get the latest state
    val reloadUser: () -> Unit = {
        currentUser = null // Force recomposition
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            currentUser = FirebaseAuth.getInstance().currentUser
        }
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            // Dynamically change the title
                            when {
                                currentUser == null -> "Citizen Login"
                                !currentUser!!.isEmailVerified -> "Verify Your Email"
                                else -> "Profile Settings"
                            },
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (currentUser == null) {
                // --- STATE 1: Logged Out ---
                AuthScreen(
                    onLoginSuccess = {
                        currentUser = FirebaseAuth.getInstance().currentUser
                    }
                )
            } else if (!currentUser!!.isEmailVerified) {
                // --- STATE 2: Logged In, but Email NOT Verified ---
                EmailVerificationScreen(
                    onRefresh = reloadUser,
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                    }
                )
            } else {
                // --- STATE 3: Logged In and Email Verified ---
                LoggedInProfileContent(
                    navController = navController,
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                    }
                )
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(onRefresh: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(GovColors.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.MarkEmailRead,
            contentDescription = "Email Not Verified",
            modifier = Modifier.size(64.dp),
            tint = GovColors.Warning
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Verification Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = GovColors.NavyBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your account is created, but you need to verify your email address before you can access your profile. Please check your inbox for the verification link.",
            textAlign = TextAlign.Center,
            color = GovColors.DarkGray
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("I've Verified, Refresh Status", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                isSending = true
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        isSending = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Verification email sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to send email. Try again later.", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            enabled = !isSending,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, GovColors.NavyBlue)
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Resend Verification Email", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSignOut) {
            Text("Sign Out", color = GovColors.Error)
        }
    }
}

@Composable
fun LoggedInProfileContent(navController: NavController, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val name = currentUser?.displayName ?: "Citizen"
    val email = currentUser?.email ?: "No email provided"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GovColors.LightGray)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Profile Header Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GovColors.NavyBlue, GovColors.AccentBlue)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "C",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovColors.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovColors.NavyBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GovColors.DarkGray
                )
            }
        }

        // --- Account Settings Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Default.Person, text = "Edit Personal Information") {
                    navController.navigate("edit_profile")
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Notifications, text = "Notification Settings") {
                    Toast.makeText(context, "Navigate to Notification Settings", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Lock, text = "Change Password") {
                    Toast.makeText(context, "Navigate to Change Password Screen", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- More Information Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.AutoMirrored.Filled.Help, text = "Help & Support") {
                    Toast.makeText(context, "Navigate to Help & Support", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Shield, text = "Privacy Policy") {
                    Toast.makeText(context, "Navigate to Privacy Policy", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- Sign Out Button ---
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GovColors.Error,
                contentColor = GovColors.White
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = GovColors.AccentBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = GovColors.NavyBlue,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = GovColors.DarkGray
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf("") } // You'd typically load this from a database like Firestore
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Information",
                            color = GovColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovColors.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GovColors.LightGray)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Update Your Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GovColors.NavyBlue
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = { /* Email is read-only */ },
                        label = { Text("Verified Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = GovColors.DarkGray,
                            disabledBorderColor = GovColors.DarkGray.copy(alpha = 0.3f),
                            disabledLabelColor = GovColors.DarkGray.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (displayName.isBlank()) {
                                Toast.makeText(context, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_LONG).show()
                                    }
                                }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GovColors.Success,
                            contentColor = GovColors.White
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GovColors.White)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CityConnectTheme {
        CityConnectApp()
    }
}

