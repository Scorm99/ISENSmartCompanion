package fr.isen.faury.isensmartcompanion.model

import java.io.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event (
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String,
    val isNotificationEnabled: Boolean = true): Parcelable {
        companion object {
            fun fakeEvents(): List<Event> {
                return listOf(
                    Event("1", "Soirée BDE", "Une soirée organisée par le BDE.", "10/03/2025", "Salle des fêtes", "Fête", true),
                    Event("2", "Conférence Tech", "Conférence sur l’IA et la cybersécurité.", "15/03/2025", "Auditorium ISEN", "Éducation", false),
                    Event("3", "Tournoi de foot", "Tournoi organisé par le BDS.", "20/03/2025", "Stade municipal", "Sport", true)
                )
            }
        }
    }