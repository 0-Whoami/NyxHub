package com.nyxhub.presentation

import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val apiUrl = "https://api.github.com/repos/0-Whoami/nyx_presets/contents"

fun getData(apiUrl: String, onSuccess: (JSONArray) -> Unit):NetWorkResponse{

    return download(apiUrl) { stream ->
        val reader = BufferedReader(InputStreamReader(stream))
        val response = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        onSuccess(JSONArray(response.toString()))
    }
}
fun download(url:String,onFailure: ()->Unit={}, onSuccess:(InputStream)->Unit):NetWorkResponse{
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use(onSuccess)
            connection.disconnect()
            return NetWorkResponse.Success
        } else {
            println("Error:$responseCode unable to connect $url")
            onFailure()
            connection.disconnect()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        onFailure()
        println(url)
    }
    return NetWorkResponse.Failed
}