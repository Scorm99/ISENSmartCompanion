package fr.isen.faury.isensmartcompanion.api

import fr.isen.faury.isensmartcompanion.model.Event
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}