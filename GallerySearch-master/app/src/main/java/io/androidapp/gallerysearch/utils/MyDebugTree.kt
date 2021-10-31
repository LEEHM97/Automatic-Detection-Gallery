package io.androidapp.gallerysearch.utils

import timber.log.Timber

class MyDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "[C:%s] [L:%s] [M:%s] ",
            super.createStackElementTag(element),
            element.lineNumber,
            element.methodName
        )
    }
}