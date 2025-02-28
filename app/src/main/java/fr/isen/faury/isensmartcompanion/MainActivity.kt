package fr.isen.faury.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.faury.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import fr.isen.faury.isensmartcompanion.ui.HistoryScreen
import fr.isen.faury.isensmartcompanion.ui.EventsScreen
import fr.isen.faury.isensmartcompanion.ui.GeminiViewModel
import fr.isen.faury.isensmartcompanion.ui.HistoryViewModel
import fr.isen.faury.isensmartcompanion.ui.EventsViewModel
import fr.isen.faury.isensmartcompanion.ui.ChatMessage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch
import fr.isen.faury.isensmartcompanion.ui.EventPreferencesManager
import fr.isen.faury.isensmartcompanion.ui.NotificationScheduler
import fr.isen.faury.isensmartcompanion.model.Event
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.util.Log
import android.content.Context

class MainActivity : ComponentActivity() {
    private lateinit var eventPreferencesManager: EventPreferencesManager

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications for event reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("event_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("Notification", "Canal de notification créé ✅")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        eventPreferencesManager = EventPreferencesManager(this)

        requestNotificationPermission()

        createNotificationChannel()

        setContent {
            ISENSmartCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(application)

                    LaunchedEffect(Unit) {
                        val events = listOf(
                            Event(id = "1", title = "Réunion", description = "Ne pas oublier la réunion !", date = "21/03/2025", location = "amphithéâtre", category = "Administration", isNotificationEnabled = true)
                        )

                        events.forEach { event ->
                            eventPreferencesManager.getEventNotificationPreference(event.id).collect { isNotified ->
                                if (isNotified) {
                                    NotificationScheduler.scheduleNotification(this@MainActivity, event)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
}

@Composable
fun MainApp(application: android.app.Application) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val geminiViewModel: GeminiViewModel = viewModel(
                    factory = GeminiViewModelFactory(application)
                )
                MainScreen(geminiViewModel)
            }
            composable("events") {
                val eventsViewModel: EventsViewModel = viewModel()
                EventsScreen(viewModel = eventsViewModel)
            }
            composable("history") {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(application)
                )
                HistoryScreen(historyViewModel)
            }
        }
    }
}

class GeminiViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HistoryViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun MainScreen(viewModel: GeminiViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var question by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.isen),
            contentDescription = "Logo ISEN",
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp)
        )

        Text(
            text = "ISEN Smart Companion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }
        }

        LaunchedEffect(chatMessages.size) {
            if (chatMessages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                placeholder = { Text("Poser une question") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (question.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(question)
                            question = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    if (question.isNotBlank() && !isLoading) {
                        viewModel.sendMessage(question)
                        question = ""
                        keyboardController?.hide()
                    }
                },
                enabled = !isLoading && question.isNotBlank(),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUserMessage)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (message.isUserMessage)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    val bubbleAlignment = if (message.isUserMessage) Alignment.CenterEnd else Alignment.CenterStart


    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = bubbleAlignment
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("home", "Accueil", Icons.Default.Home),
            BottomNavItem("events", "Évènements", Icons.Default.Notifications),
            BottomNavItem("history", "Historique", Icons.Default.Settings)
        )

        val currentRoute = currentRoute(navController)

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

fun currentRoute(navController: NavController): String? {
    val navBackStackEntry = navController.currentBackStackEntry
    return navBackStackEntry?.destination?.route
}

