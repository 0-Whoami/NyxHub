package com.nyxhub.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File
import java.util.Stack

@Immutable
class Properties(file_path : String) {
    val prop = SnapshotStateList<Entry>()
    private val stack = Stack<() -> Unit>()
    private val file = File(file_path)

    init {
        if (file.exists()) {
            file.forEachLine { line ->
                line.split(" : ").let { strings ->
                    if (strings.size == 2) prop.add(Entry(strings[0], strings[1]))
                }
            }
        }
    }

    private fun add(element : Entry) {
        val entry = getEntry(element.key)
        if (entry == null) {
            stack.push { prop.remove(element) }
            prop.add(element)
        }
    }

    fun update(oldElement : Entry?, newElement : Entry) {
        val i = prop.indexOf(oldElement)
        if (i != -1) {
            stack.push {
                prop[i] = oldElement!!
            }
            prop[i] = newElement
        } else {
            add(newElement)
        }
    }

    fun update(key : String, newValue : Any?) {
        val entry = getEntry(key)
        update(entry, Entry(key, newValue.toString()))
    }

    fun getEntry(key : String) : Entry? {
        prop.forEach {
            if (it.key == key) return it
        }
        return null
    }

    fun get(key : String) : String? {
        prop.forEach {
            if (it.key == key) return it.value
        }
        return null
    }

    fun getInt(key : String, default : Int) : Int {
        with(get(key)) {
            if (this != null) return this.toInt()
        }
        return default
    }

    fun getBoolean(key : String, default : Boolean) : Boolean {
        with(get(key)) {
            if (this != null) return this.toBoolean()
        }
        return default
    }

    fun remove(element : Entry) : Boolean {
        val removedValue = prop.remove(element)
        if (removedValue) {
            stack.push { prop.add(element) }
        }
        return removedValue
    }

    fun undo() {
        if (stack.isNotEmpty()) stack.pop()()
    }

    fun save() {
        stack.clear()
        if (!file.parentFile?.exists()!!) file.parentFile?.mkdirs()
        if (!file.exists()) file.createNewFile()
        if (prop.isEmpty()) file.delete()
        file.writeText(prop.joinToString("\n") { "${it.key} : ${it.value}" })
    }
}

@Immutable
class Entry(val key : String, val value : String)