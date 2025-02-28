package fr.isen.faury.isensmartcompanion.ui

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import fr.isen.faury.isensmartcompanion.api.NetworkManager
import fr.isen.faury.isensmartcompanion.model.Event



class MainViewModel : ViewModel() {
    private val _events = mutableStateOf<List<Event>>(emptyList())
    val events: State<List<Event>> = _events

    fun fetchEvents() {
        viewModelScope.launch {
            val call = NetworkManager.api.getEvents()
            call.enqueue(object : Callback<List<Event>> {
                override fun onResponse(p0: Call<List<Event>>, p1: Response<List<Event>>) {
                    if (p1.isSuccessful) {
                        _events.value = p1.body() ?: emptyList()
                        Log.d("API", "Données reçues : ${_events.value}")
                    } else {
                        Log.e("API", "Erreur ${p1.code()}")
                    }
                }

                override fun onFailure(p0: Call<List<Event>>, p1: Throwable) {
                    Log.e("API", "Échec : ${p1.message}")
                }
            })
        }
    }
}