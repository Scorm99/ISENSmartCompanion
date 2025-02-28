package fr.isen.faury.isensmartcompanion.ui

data class ChatMessage (
    val content: String,
    val isUserMessage: Boolean = false
)