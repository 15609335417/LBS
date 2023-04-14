package com.example.test01


data class SensorData(
    var time:String="",
    var acceleraterData: FloatArray =FloatArray(3),
    var gyroscopeData: FloatArray=FloatArray(3),
    var magnetometerData : FloatArray=FloatArray(3),
    var orientation: FloatArray=FloatArray(3),
    var postionData:FloatArray=FloatArray(3)
)
