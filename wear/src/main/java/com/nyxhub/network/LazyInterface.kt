package com.nyxhub.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream

abstract class LazyInterface<T>(val name : String, private val loadFrom : String) {
    var loadable : T? by mutableStateOf(null)
    protected abstract fun decodeFromStream(inp : InputStream)
    protected abstract fun onError()
    fun load() {
        if (loadable == null) download(loadFrom, this::onError, this::decodeFromStream)
    }
}

@Composable
fun <T> NetworkLazy(coroutine : CoroutineScope, item : LazyInterface<T>, content : @Composable () -> Unit) {
    content()
    LaunchedEffect(item) {
        coroutine.launch {
            item.load()
        }
    }
}
