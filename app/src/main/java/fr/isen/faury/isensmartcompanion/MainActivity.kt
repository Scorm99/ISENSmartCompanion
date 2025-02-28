package fr.isen.faury.isensmartcompanion

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.faury.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable


data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String): Serializable {
        companion object {
            fun fakeEvents(): List<Event> {
                return listOf(
                    Event(1, "Soirée BDE", "Une soirée organisée par le BDE.", "10/03/2025", "Salle des fêtes", "Fête"),
                    Event(2, "Conférence Tech", "Conférence sur l’IA et la cybersécurité.", "15/03/2025", "Auditorium ISEN", "Éducation"),
                    Event(3, "Tournoi de foot", "Tournoi organisé par le BDS.", "20/03/2025", "Stade municipal", "Sport")
                )
            }
        }
    }








class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ISENSmartCompanionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen()
                    Navigation()
                }
            }
        }
    }
}


@Composable
fun MainScreen() {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var aiResponse by remember { mutableStateOf("Réponse de l'IA ici...") }

    val context = LocalContext.current // Contexte valide ici

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.isen),
            contentDescription = "Logo ISEN",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Titre
        Text(text = "ISEN Smart Companion", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Champ de saisie
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Poser une question") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton d'envoi
        Button(onClick = {
            Toast.makeText(context, "Question Submitted", Toast.LENGTH_SHORT).show()
            aiResponse = "Tu as demandé : ${userInput.text}"
        }) {
            Text("Envoyer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Réponse de l'IA
        Text(text = aiResponse, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun EventsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Événements à venir", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(fakeEvents) { event ->
                EventItem(event, onClick = {
                    navController.navigate("event_detail/${event.id}")
                })
            }
        }
    }
}


@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = event.date, style = MaterialTheme.typography.bodyMedium)
            Text(text = event.location, style = MaterialTheme.typography.bodySmall)
        }
    }
}



@Composable
fun EventDetailScreen(event: Event) {
    val event = fakeEvents.find { it.id.toString() == eventId }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        event?.let {
            Text(text = it.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = "📅 ${it.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "📍 ${it.location}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it.description, style = MaterialTheme.typography.bodyLarge)
        } ?: Text("Événement introuvable !")
    }
}


@Composable
fun HistoryScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Historique des interactions", style = MaterialTheme.typography.headlineMedium)
    }
}


@Composable
fun Navigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { MainScreen() }
            composable("events") { EventsScreen(navController) }
            composable("history") { HistoryScreen() }
            composable("event_detail/{eventId}") { backStackEntry -> EventDetailScreen(backStackEntry.arguments?.getString("eventId"))}
        }
    }
}



@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Events,
        Screen.History
    )

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = false, // Gérer la sélection ici
                onClick = { navController.navigate(screen.route) }
            )
        }
    }
}



sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object Events : Screen("events", "Événements", Icons.Default.Notifications)
    object History : Screen("history", "Historique", Icons.Default.Settings)
}












@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ISENSmartCompanionTheme {
        MainScreen()
    }
}