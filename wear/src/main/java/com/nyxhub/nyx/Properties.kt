package com.nyxhub.nyx

import java.io.File

class Properties(file_path: String) {
    val map = mutableMapOf<String, String>()
    private val file = File(file_path)

    init {
        if (file.exists()) {
            file.forEachLine { line ->
                line.split(" ").let {
                    map[it[0]] = it[1]
                }
            }
        }
    }
    fun put(key: Any,value:Any)=map.put(key.toString(),value.toString())
    fun getString(key: String,default: String=""): String = map[key]?:default
    fun forEach(action: (key: String, value: String) -> Unit) = map.forEach(action)
    fun getInt(key: String,default: Int): Int = if (map.containsKey(key)) map[key]!!.toInt() else default
    fun getBoolean(key: String,default: Boolean): Boolean = if (map.containsKey(key)) map[key]!!.toBoolean() else default
    fun save(){
        if (!file.parentFile?.exists()!!) file.parentFile?.mkdirs()
        if (!file.exists()) file.createNewFile()
        if (map.isEmpty()) file.delete()
        file.writeText(
            map.entries.joinToString("\n"){"${it.key} ${it.value}"}
        )
    }
    fun remove(key: Any)=map.remove(key)
}
