package fr.isen.faury.isensmartcompanion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import fr.isen.faury.isensmartcompanion.BuildConfig

class GeminiViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    val availableModels = listOf(
        "gemini-2.0-pro-exp-02-05",
        "gemini-2.0-flash-thinking-exp-01-21",
        "gemini-2.0-flash-exp",
        "gemini-2.0-flash-lite",
        "gemini-1.5-pro",
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash",

        )

    private val _selectedModel = MutableStateFlow("gemini-2.0-pro-exp-02-05")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private var generativeModel = createGenerativeModel(_selectedModel.value)

    private var chatSession = generativeModel.startChat()

    private val repository: ConversationRepository

    private val _chatMessages = MutableStateFlow(
        listOf(ChatMessage("Prêt à répondre à vos questions !", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val conversationDao = AppDatabase.getDatabase(application).conversationDao()
        repository = ConversationRepository(conversationDao)
    }

    private fun createGenerativeModel(modelName: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.6f
                topK = 50
                topP = 0.92f
                maxOutputTokens = 4096
            }
        )
    }

    fun setModel(modelName: String) {
        if (modelName in availableModels) {
            _selectedModel.value = modelName
            generativeModel = createGenerativeModel(modelName)

            chatSession = generativeModel.startChat()

            _chatMessages.value += ChatMessage("Modèle changé pour $modelName. L'historique de conversation a été réinitialisé.", false)
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        _chatMessages.value += ChatMessage(userMessage, true)

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chatSession.sendMessage(userMessage)
                val responseText = response.text ?: "Désolé, je n'ai pas pu générer de réponse."

                _chatMessages.value += ChatMessage(responseText, false)

                repository.insertConversation(userMessage, responseText)
            } catch (e: Exception) {
                val errorMessage = "Erreur: ${e.localizedMessage ?: "Impossible de communiquer avec Gemini"}"
                _chatMessages.value += ChatMessage(errorMessage, false)

                repository.insertConversation(userMessage, errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearConversation() {
        chatSession = generativeModel.startChat()
        _chatMessages.value = listOf(ChatMessage("Conversation réinitialisée. Prêt à répondre à vos questions !", false))
    }
}








