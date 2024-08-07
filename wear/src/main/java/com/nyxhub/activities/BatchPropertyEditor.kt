package com.nyxhub.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import com.nyxhub.base.Card
import com.nyxhub.base.Heading
import com.nyxhub.base.LazyListWrapper
import com.nyxhub.base.SwappableCard
import com.nyxhub.base.Text
import com.nyxhub.base.VerticalDivider
import com.nyxhub.base.cornerDotBorder
import com.nyxhub.data.Properties
import com.nyxhub.presentation.primary_color
import nyx.constants.Constant

class BatchPropertyEditor : ComponentActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        val path = intent.getStringExtra(PATH) ?: Constant.EXTRA_KEYS_CONFIG
        val enableColorPreview = path == Constant.EXTRA_COLORS_CONFIG
        val properties = Properties(path)
        val propertiesList = properties.prop
        val textModifier = Modifier.padding(10.dp)

        setContent {
            LazyListWrapper { center ->
                item { Text() }
                item { Heading(if (enableColorPreview) "TERM COLORS" else "EXTRA KEYS") }
                itemsIndexed(propertiesList) { i, it ->
                    SwappableCard(deleteAction = { properties.remove(it) },
                                  editAction = { EntryEditor.editEntry(this@BatchPropertyEditor, it, properties) }) {
                        val color = remember {
                            if (enableColorPreview) try {
                                return@remember Color(android.graphics.Color.parseColor(it.value))
                            } catch (_ : Exception) {
                            }
                            null
                        }
                        VerticalDivider()
                        Card(text = it.value, click = { EntryEditor.editEntry(this@BatchPropertyEditor, it, properties) }, focused = i == center) {
                            if (color != null) Box(Modifier
                                                       .size(50.dp)
                                                       .padding(7.dp)
                                                       .cornerDotBorder()
                                                       .padding(3.dp)
                                                       .background(color))
                        }

                    }
                }
                item {
                    Row(Modifier
                            .padding(top = 10.dp)
                            .border(1.dp, primary_color), verticalAlignment = Alignment.CenterVertically) {
                        Text("ADD", modifier = textModifier
                            .weight(1f)
                            .clickable { EntryEditor.addEntry(this@BatchPropertyEditor, properties) })
                        VerticalDivider()
                        Text("SAVE", modifier = textModifier.clickable(onClick = properties::save))
                        VerticalDivider()
                        Text("UNDO", modifier = textModifier.clickable(onClick = properties::undo))
                    }
                }
            }
        }
        super.onCreate(savedInstanceState)
    }


    companion object {
        private const val PATH = "path"
        private fun editBatchProp(context : Context, s : String) {
            context.startActivity(Intent(context, BatchPropertyEditor::class.java).putExtra(PATH, s))
        }

        fun editKeys(context : Context) {
            editBatchProp(context, Constant.EXTRA_KEYS_CONFIG)
        }

        fun editColors(context : Context) {
            editBatchProp(context, Constant.EXTRA_COLORS_CONFIG)
        }
    }
}