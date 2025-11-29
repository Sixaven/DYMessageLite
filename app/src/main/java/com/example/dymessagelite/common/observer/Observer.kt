package com.example.dymessagelite.common.observer

interface Observer<T>{
    fun update(data: T)
}

interface Subject<T>{
    fun addObserver(observer: Observer<T>)
    fun removeObserver(observer: Observer<T>)
    fun notifyObservers(data: T)
}