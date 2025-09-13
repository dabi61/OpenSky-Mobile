package com.dabi.opensky.core.viewmodel

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val key: ViewModelKey = ViewModelKey(this::class.java.name)

    protected fun <T> BaseViewModel.viewModelStateFlow(value: T): ViewModelStateFlow<T> {
        return ViewModelStateFlow(key = key, value = value)
    }
}
