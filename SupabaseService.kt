package com.example.data.remote

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseService(
    private var baseUrl: String,
    private var apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun updateConfig(url: String, key: String) {
        this.baseUrl = url.trimEnd('/')
        this.apiKey = key
    }

    fun isConfigured(): Boolean {
        return baseUrl.isNotBlank() &&
                apiKey.isNotBlank() &&
                !baseUrl.contains("your-project-url") &&
                !apiKey.contains("your-anon-public-key")
    }

    suspend fun getTable(tableName: String, selectQuery: String = "*"): String? {
        if (!isConfigured()) return null
        val url = "$baseUrl/rest/v1/$tableName?select=$selectQuery"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.e("SupabaseService", "getTable $tableName error ${response.code}: ${response.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "getTable $tableName exception: ${e.message}")
            null
        }
    }

    suspend fun upsertTable(tableName: String, jsonArray: JSONArray): Boolean {
        if (!isConfigured()) return false
        val url = "$baseUrl/rest/v1/$tableName"
        val mediaType = "application/json".toMediaType()
        val body = jsonArray.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("SupabaseService", "upsertTable $tableName error ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "upsertTable $tableName exception: ${e.message}")
            false
        }
    }

    suspend fun deleteRow(tableName: String, idColumn: String, idValue: Any): Boolean {
        if (!isConfigured()) return false
        val url = "$baseUrl/rest/v1/$tableName?$idColumn=eq.$idValue"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .delete()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}
