package com.nyxhub.presentation

import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun main() {
    print()
}
fun print(){
    val owner = "0-Whoami"
    val repo = "nyx_presets"
    val token = "TOKEN" // Replace with your actual PAT

    val apiUrl = "https://api.github.com/repos/$owner/$repo/contents"
    val connection = URL(apiUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Authorization", "Bearer $token")

    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        reader.close()
        inputStream.close()

        // Parse the JSON response
        val jsonArray = JSONArray(response.toString())
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getString("name")
            println(item.getString("type"))
            println("File/Folder: $name")
        }
    } else {
        println("Error fetching repository contents: $responseCode")
    }

    connection.disconnect()
}