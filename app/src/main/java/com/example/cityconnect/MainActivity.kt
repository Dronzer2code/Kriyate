package com.example.cityconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.example.cityconnect.ui.theme.CityConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

data class Report(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "",
    val imageData: String = ""
)


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
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Citizen"
    val displayInitial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    val citizenId = remember(currentUser) {
        currentUser?.uid?.let { uid ->
            "CZ-${uid.takeLast(6).uppercase()}"
        } ?: "CZ-..."
    }

    var openReportsCount by remember { mutableStateOf(0) }
    var recentlyUpdatedCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null) {
            isLoading = true
            db.collection("reports")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val reports = documents.toObjects(Report::class.java)
                    openReportsCount = reports.count { it.status != "Resolved" }
                    recentlyUpdatedCount = reports.count { it.status == "In Progress" || it.status == "Acknowledged" }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Kriyaté",
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
                            "Citizen ID: $citizenId",
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
                subtitle = "$openReportsCount reports pending review",
                count = if (isLoading) ".." else openReportsCount.toString(),
                color = GovColors.Warning
            ) {
                navController.navigate("my_reports")
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReportSummaryItem(
                icon = Icons.Default.Refresh,
                title = "Recently Updated",
                subtitle = "$recentlyUpdatedCount reports with new updates",
                count = if (isLoading) ".." else recentlyUpdatedCount.toString(),
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

fun createImageFile(context: Context): Uri {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir: File? = context.cacheDir
    val file = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(navController: NavController) {
    val reportCategories = listOf(
        "Pothole / Road Damage", "Streetlight Outage", "Garbage / Illegal Dumping",
        "Water Leakage / Supply Issue", "Blocked Drains / Sewage", "Fallen Trees / Debris",
        "Broken Pavement / Sidewalk", "Public Property Vandalism", "Parking Violation",
        "Stray Animal Issue", "Noise Complaint", "Other"
    )

    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = tempImageUri
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                tempImageUri = createImageFile(context)
                cameraLauncher.launch(tempImageUri)
            } else {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = { Text("Submit New Report", color = GovColors.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GovColors.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Add Photo") },
                text = { Text("Choose a source for your photo.") },
                confirmButton = {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                            PackageManager.PERMISSION_GRANTED -> {
                                tempImageUri = createImageFile(context)
                                cameraLauncher.launch(tempImageUri)
                            }
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Camera")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text("Gallery")
                    }
                }
            )
        }

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
                    modifier = Modifier.fillMaxWidth().shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Add Supporting Media", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(0.dp))
                                .background(GovColors.LightGray)
                                .border(2.dp, GovColors.AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                                .clickable { showImageSourceDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUri),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { imageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, "Remove Image", tint = Color.White)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, "Upload", tint = GovColors.AccentBlue, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Add a Photo", color = GovColors.DarkGray, fontWeight = FontWeight.Medium)
                                    Text("Camera or Gallery", color = GovColors.DarkGray.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Location Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
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
                            modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(0.dp)).background(GovColors.LightGray)
                        ) {
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
                    modifier = Modifier.fillMaxWidth().shadow(4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = GovColors.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Issue Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = isCategoryExpanded,
                            onExpandedChange = { isCategoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GovColors.AccentBlue,
                                    focusedLabelColor = GovColors.AccentBlue
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(0.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = isCategoryExpanded,
                                onDismissRequest = { isCategoryExpanded = false }
                            ) {
                                reportCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedCategory = category
                                            isCategoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
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
                    onClick = {
                        if (selectedCategory.isBlank() || location.isBlank()) {
                            Toast.makeText(context, "Please select a category and provide a location.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(context, "You must be logged in.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        isLoading = true
                        var imageDataString = ""
                        if (imageUri != null) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                                val bytes = inputStream?.readBytes()
                                inputStream?.close()
                                if (bytes != null) {
                                    if (bytes.size > 1_000_000) { // 1MB Check
                                        Toast.makeText(context, "Image is too large (Max 1MB).", Toast.LENGTH_LONG).show()
                                        isLoading = false
                                        return@Button
                                    }
                                    imageDataString = Base64.encodeToString(bytes, Base64.DEFAULT)
                                }
                            } catch (e: Exception) {
                                Log.e("ImageConversion", "Error converting image to Base64", e)
                                Toast.makeText(context, "Could not process image.", Toast.LENGTH_LONG).show()
                                isLoading = false
                                return@Button
                            }
                        }

                        saveReportToFirestore(db, user.uid, user.displayName, selectedCategory, description, location, imageDataString, navController) { success ->
                            isLoading = false
                            if (!success) {
                                Toast.makeText(context, "Failed to save report.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(6.dp, RoundedCornerShape(0.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = GovColors.Success, contentColor = GovColors.White),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GovColors.White, strokeWidth = 3.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

fun saveReportToFirestore(
    db: FirebaseFirestore,
    userId: String,
    userName: String?,
    category: String,
    description: String,
    location: String,
    imageData: String,
    navController: NavController,
    onComplete: (Boolean) -> Unit
) {
    val reportData = hashMapOf(
        "userId" to userId,
        "userName" to (userName ?: "Anonymous"),
        "category" to category,
        "description" to description,
        "location" to location,
        "timestamp" to Timestamp.now(),
        "status" to "Submitted",
        "imageData" to imageData
    )
    db.collection("reports")
        .add(reportData)
        .addOnSuccessListener {
            Log.d("Firestore", "Report added with ID: ${it.id}")
            navController.navigate("report_submitted")
            onComplete(true)
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding report", e)
            onComplete(false)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSubmittedScreen(navController: NavController) {
    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = { Text("Report Submitted", color = GovColors.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(GovColors.LightGray).padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).background(GovColors.Success.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, "Success", tint = GovColors.Success, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Report Submitted Successfully", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your report has been received and assigned a tracking ID. You will receive updates on its progress.", style = MaterialTheme.typography.bodyMedium, color = GovColors.DarkGray, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovColors.NavyBlue, contentColor = GovColors.White),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Return to Home", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(navController: NavController) {
    var reportsList by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(key1 = auth.currentUser) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("reports")
                .whereEqualTo("userId", user.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    reportsList = result.documents.mapNotNull { doc ->
                        doc.toObject<Report>()?.copy(id = doc.id)
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("MyReportsScreen", "Firestore failure: ", exception)
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = { Text("My Reports", color = GovColors.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(GovColors.LightGray).padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (reportsList.isEmpty()) {
                Text("You have not submitted any reports yet.", modifier = Modifier.align(Alignment.Center).padding(16.dp), color = GovColors.DarkGray, textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportsList) { report ->
                        ReportItem(report) {
                            navController.navigate("report_details/${report.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).shadow(4.dp),
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
                    Text(report.location, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.category, style = MaterialTheme.typography.bodyMedium, color = GovColors.DarkGray)
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
        Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(navController: NavController, reportId: String?) {
    var report by remember { mutableStateOf<Report?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = reportId) {
        if (reportId == null) {
            isLoading = false
            return@LaunchedEffect
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("reports").document(reportId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    report = document.toObject<Report>()?.copy(id = document.id)
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = { Text("Report Details", color = GovColors.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GovColors.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(GovColors.LightGray).padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (report != null) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val bitmap = remember(report!!.imageData) {
                        if (report!!.imageData.isNotBlank()) {
                            try {
                                val imageBytes = Base64.decode(report!!.imageData, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) {
                                Log.e("ImageDecode", "Error decoding Base64 image", e)
                                null
                            }
                        } else {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Report Image",
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        PlaceholderImage()
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(4.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = GovColors.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Report #${report!!.id}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                                    StatusChip(report!!.status)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(report!!.location, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(4.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = GovColors.White)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailRow(Icons.Default.LocationOn, "Location", report!!.location)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow(Icons.AutoMirrored.Filled.List, "Category", report!!.category)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow(Icons.Default.Info, "Description", report!!.description)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Text("Report not found.", modifier = Modifier.align(Alignment.Center), color = GovColors.DarkGray)
            }
        }
    }
}

@Composable
fun PlaceholderImage() {
    Box(
        modifier = Modifier.fillMaxWidth().height(250.dp).background(GovColors.DarkGray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ImageNotSupported,
            contentDescription = "No Image Provided",
            tint = GovColors.DarkGray.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, label, tint = GovColors.AccentBlue, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = GovColors.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    val reloadUser: () -> Unit = {
        currentUser = null
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            currentUser = FirebaseAuth.getInstance().currentUser
        }
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = {
                        Text(
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (currentUser == null) {
                AuthScreen(onLoginSuccess = { currentUser = FirebaseAuth.getInstance().currentUser })
            } else if (!currentUser!!.isEmailVerified) {
                EmailVerificationScreen(
                    onRefresh = reloadUser,
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                    }
                )
            } else {
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
        modifier = Modifier.fillMaxSize().padding(24.dp).background(GovColors.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.MarkEmailRead, "Email Not Verified", modifier = Modifier.size(64.dp), tint = GovColors.Warning)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Verification Required", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Your account is created, but you need to verify your email address before you can access your profile. Please check your inbox for the verification link.", textAlign = TextAlign.Center, color = GovColors.DarkGray)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Icon(Icons.Default.Refresh, "Refresh")
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
            modifier = Modifier.fillMaxWidth().height(48.dp),
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
        modifier = Modifier.fillMaxSize().background(GovColors.LightGray).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(brush = Brush.linearGradient(colors = listOf(GovColors.NavyBlue, GovColors.AccentBlue)), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = name.firstOrNull()?.uppercase() ?: "C", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = GovColors.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = email, style = MaterialTheme.typography.bodyMedium, color = GovColors.DarkGray)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Default.Person, text = "Edit Personal Information") { navController.navigate("edit_profile") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Notifications, text = "Notification Settings") { Toast.makeText(context, "Nav to Notification Settings", Toast.LENGTH_SHORT).show() }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Lock, text = "Change Password") { Toast.makeText(context, "Nav to Change Password", Toast.LENGTH_SHORT).show() }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = GovColors.White)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.AutoMirrored.Filled.Help, text = "Help & Support") { Toast.makeText(context, "Nav to Help & Support", Toast.LENGTH_SHORT).show() }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(icon = Icons.Default.Shield, text = "Privacy Policy") { Toast.makeText(context, "Nav to Privacy Policy", Toast.LENGTH_SHORT).show() }
            }
        }

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GovColors.Error, contentColor = GovColors.White),
            shape = RoundedCornerShape(0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sign Out")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, text, tint = GovColors.AccentBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = GovColors.NavyBlue, fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = GovColors.DarkGray)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.NavyBlue)
            ) {
                TopAppBar(
                    title = { Text("Edit Information", color = GovColors.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GovColors.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(GovColors.LightGray).padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = GovColors.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Update Your Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GovColors.NavyBlue)
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), singleLine = true)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = user?.email ?: "", onValueChange = { /* Email is read-only */ }, label = { Text("Verified Email Address") }, modifier = Modifier.fillMaxWidth(), readOnly = true, shape = RoundedCornerShape(0.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = GovColors.DarkGray, disabledBorderColor = GovColors.DarkGray.copy(alpha = 0.3f), disabledLabelColor = GovColors.DarkGray.copy(alpha = 0.7f)))
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (displayName.isBlank()) {
                                Toast.makeText(context, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
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
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GovColors.Success, contentColor = GovColors.White),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GovColors.White)
                        } else {
                            Icon(Icons.Default.Check, null)
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

