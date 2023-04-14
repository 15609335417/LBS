package com.example.test01

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.example.test01.databinding.FragmentDataBinding


class MyLocationListener(var sensorData: SensorData,var binding: FragmentDataBinding):LocationListener {
    override fun onLocationChanged(location: Location) {
        //TODO("Not yet implemented")

//        binding.Lat.text="Lat =${location.latitude}"
//        binding.Lon.text="Lon =${location.longitude}"
//        binding.H.text  ="H   =${location.altitude}"
//        sensorData.postionData[0]=location.latitude.toFloat()
//        sensorData.postionData[1]=location.longitude.toFloat()
//        sensorData.postionData[2]=location.altitude.toFloat()

    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

//    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        super.onStatusChanged(provider, status, extras)
//    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }
}