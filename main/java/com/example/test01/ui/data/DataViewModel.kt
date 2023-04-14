package com.example.test01.ui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.test01.MainActivity

class DataViewModel : ViewModel() {

    private val _acclX = MutableLiveData<String>().apply {
        value = MainActivity().sensorData.acceleraterData[0].toString()
    }
    val acclX: LiveData<String> = _acclX
    private val _acclY = MutableLiveData<String>().apply {
        value = MainActivity().sensorData.acceleraterData[1].toString()
    }
    val acclY: LiveData<String> = _acclY
    private val _acclZ = MutableLiveData<String>().apply {
        value = MainActivity().sensorData.acceleraterData[2].toString()
    }
    val acclZ: LiveData<String> = _acclZ
}