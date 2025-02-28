package fr.isen.faury.isensmartcompanion.ui

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.example.com/"  // Remplace par l'URL de ton API Gemini

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // Utilise Gson pour la conversion JSON
        .client(OkHttpClient.Builder().build())  // Crée un OkHttpClient pour gérer la connexion
        .build()
}