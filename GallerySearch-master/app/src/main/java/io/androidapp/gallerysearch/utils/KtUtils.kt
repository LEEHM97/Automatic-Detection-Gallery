package io.androidapp.gallerysearch.utils

object KtUtils {
    fun joinToString(list: Collection<String>, separator: String): String {
        return list.filter { it.isNotBlank() }.joinToString(separator)
    }
}