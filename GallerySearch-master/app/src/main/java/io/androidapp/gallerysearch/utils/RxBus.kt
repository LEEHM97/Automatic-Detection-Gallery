package io.androidapp.gallerysearch.utils

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject

object RxBus {
    val instance = RxBus
    private val subjectMap = HashMap<String, PublishSubject<Any>>()

    fun sendEvent(any: Any, key: String = "RxBus") {
        subjectMap[key]?.onNext(any)
    }

    fun receiveEvent(key: String = "RxBus"): PublishSubject<Any> {
        synchronized(this) {
            if (subjectMap.containsKey(key).not()) {
                subjectMap[key] = PublishSubject.create()
            }
            return subjectMap[key]!!
        }
    }

    fun sendDeleteEvent(path: String) {
        sendEvent(path, "delete")
    }

    fun receiveDeleteEvent() =
        receiveEvent("delete").observeOn(AndroidSchedulers.mainThread()).cast(String::class.java)!!
}