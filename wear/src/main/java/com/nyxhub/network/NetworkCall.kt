package com.nyxhub.network

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


const val apiUrl = "https://api.github.com/repos/0-Whoami/nyx_presets/contents"
const val download_url = "download_url"
const val name = "name"
const val type = "type"
fun getJsonData(apiUrl : String, onSuccess : (JSONObject) -> Unit) {
    download(apiUrl) { stream ->
        val reader = BufferedReader(InputStreamReader(stream))
        val response = StringBuilder()

        var line : String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        val jsonArray = JSONArray(response.toString())
        for (i in 0..<jsonArray.length()) onSuccess(jsonArray.getJSONObject(i))
    }
}

fun download(url : String, onFailure : () -> Unit = {}, onSuccess : (InputStream) -> Unit) {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use(onSuccess)
            connection.disconnect()
        } else {
            onFailure()
            connection.disconnect()
        }

    } catch (e : Exception) {
        onFailure()
        e.printStackTrace()
    }
}