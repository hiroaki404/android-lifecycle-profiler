package com.hiroaki404.sample

import androidx.lifecycle.ViewModel
import com.hiroaki404.lifecycle.LogLifecycle

@LogLifecycle
class MainViewModel : ViewModel() {
    override fun onCleared() {
        super.onCleared()
    }
}
