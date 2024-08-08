package com.nyxhub.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.nyxhub.base.Text
import com.nyxhub.base.TextField
import com.nyxhub.base.primary_color
import com.nyxhub.base.surfaceColor
import com.nyxhub.data.Entry
import com.nyxhub.data.Properties

class EntryEditor : ComponentActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        val entry = element ?: Entry("", "")
        setContent {
            var key by remember { mutableStateOf(entry.key) }
            var value by remember { mutableStateOf(entry.value) }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxRectangle()) {
                Text()
                Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("KEY :", modifier = Modifier.padding(10.dp))
                    TextField(text = key, onValueChanged = { key = it }, keyboardType = KeyboardType.Number, readOnly = readOnlyKey)
                }
                Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("VAL :", modifier = Modifier.padding(10.dp))
                    TextField(text = value, onValueChanged = { value = it })
                }
                Text("SAVE", modifier = Modifier
                    .fillMaxWidth()
                    .background(primary_color)
                    .padding(10.dp)
                    .clickable {
                        if (key.isNotBlank()) {
                            properties.update(element, Entry(key, value))
                        }
                        finish()
                    }, color = surfaceColor)

            }
        }
        super.onCreate(savedInstanceState)
    }

    companion object {
        private var element : Entry? = null
        private lateinit var properties : Properties
        private var readOnlyKey : Boolean = false
        fun editEntry(context : Context, element : Entry?, properties : Properties, readOnlyKey : Boolean = false) {
            this.element = element
            this.properties = properties
            this.readOnlyKey = readOnlyKey
            start(context)
        }

        fun editEntry(context : Context, key : String, defaultValue : Any?, properties : Properties, readOnlyKey : Boolean = true) {
            editEntry(context, properties.getEntry(key) ?: Entry(key, defaultValue.toString()), properties, readOnlyKey)
        }

        fun addEntry(context : Context, properties : Properties) {
            editEntry(context, null, properties)
        }

        private fun start(context : Context) {
            context.startActivity(Intent(context, EntryEditor::class.java))
        }
    }
}