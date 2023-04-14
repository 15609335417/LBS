package com.example.test01

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.SystemClock
import android.text.method.ScrollingMovementMethod
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolylineOptions
import com.example.test01.databinding.FragmentDataBinding
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class MySensorEventListener(private val context: Context, var sensorData: SensorData,var dataBinding: FragmentDataBinding):SensorEventListener {
    val TAG="Sensor data"
    var gravity=FloatArray(3)
    //传感器数据获取时间格式设置
    val formatter = SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S", Locale.CHINA)
    val df = SimpleDateFormat( "mm ss.SSSSS", Locale.CHINA)
    var filetime=""//文件写入时间

    //数据处理
    var accl=FloatArray(dataNum)//原始数据总的加速度数组
    var yawbefore=FloatArray(dataNum)//原始数据的航向角
    var acclSmooth=FloatArray(dataNum)//平滑后的加速度
    var yawSmooth=FloatArray(dataNum)//平滑后的航向角
    var step=false//是否是脚步
    var stepint:Int=0
    var stepTime=FloatArray(3)//脚步出现的前前时刻， 前一时刻，这一时刻
    var stepLength=0.0F
    var yaw=0.0F
    var NEk1=FloatArray(2)//上一时刻的NE

    override fun onSensorChanged(event: SensorEvent) {
        var timemillis=System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000L
        //if(!isSensor)return
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            //以陀螺仪为准
            //传感器数据获取时间
            formatter.timeZone= TimeZone.getFrozenTimeZone("UTC")
            sensorData.time=formatter.format(timemillis+3600000*8).toString()
            //传感器数据保存时间
            df.timeZone= TimeZone.getFrozenTimeZone("UTC")
            filetime=df.format(timemillis+3600000*8)
            now=filetime.toString().substring(0,2).toFloat()*60+filetime.toString().substring(3).toFloat()

            sensorData.gyroscopeData = event.values.copyOf()
            Log.d(TAG, "gyro: [x=${sensorData.gyroscopeData[0]} y=${sensorData.gyroscopeData[1]} z=${sensorData.gyroscopeData[2]}]")
            dataBinding.gyrotime.text="${sensorData.time}"
            dataBinding.gyroX.text="GyroX=\n${event.values[0]}"
            dataBinding.gyroY.text="GyroY=\n${event.values[1]}"
            dataBinding.gyroZ.text="GyroZ=\n${event.values[2]}"

        }else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            sensorData.acceleraterData=event.values.copyOf()
            Log.d(TAG, "accl: [x=${sensorData.acceleraterData[0]} y=${sensorData.acceleraterData[1]} z=${sensorData.acceleraterData[2]}]")
            dataBinding.accltime.text="${sensorData.time}"
            dataBinding.acclX.text="AcclX=\n${event.values[0]}"
            dataBinding.acclY.text="AcclY=\n${event.values[1]}"
            dataBinding.acclZ.text="AcclZ=\n${event.values[2]}"
        }else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorData.magnetometerData=event.values.copyOf()
            Log.d(TAG, "magn: [x=${sensorData.magnetometerData[0]} y=${sensorData.magnetometerData[1]} z=${sensorData.magnetometerData[2]}]")
            dataBinding.magntime.text="${sensorData.time}"
            dataBinding.magnX.text="MagnX=\n${event.values[0]}"
            dataBinding.magnY.text="MagnY=\n${event.values[1]}"
            dataBinding.magnZ.text="MagnZ=\n${event.values[2]}"
        } else if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            gravity = event.values
        }
        calculateOrientation()
        if(now== last){
            return
        }else last=now
        //解算
        //数据保存到数组
        sensorDataArray[rows]= floatArrayOf(
            sensorData.acceleraterData[0],sensorData.acceleraterData[1],sensorData.acceleraterData[2],
            sensorData.gyroscopeData[0], sensorData.gyroscopeData[1], sensorData.gyroscopeData[2],
            sensorData.magnetometerData[0], sensorData.magnetometerData[1], sensorData.magnetometerData[2],
            sensorData.orientation[0],sensorData.orientation[1],sensorData.orientation[2]
            //sensorData.postionData[0],sensorData.postionData[1],sensorData.postionData[2],

            )

        acclDataArray[rows]= floatArrayOf(
            now,
            sqrt(
            sensorDataArray[rows][0] * sensorDataArray[rows][0]
                    + sensorDataArray[rows][1] * sensorDataArray[rows][1]
                    + sensorDataArray[rows][2] * sensorDataArray[rows][2]
            )
       )
        if(rows<dataNum-1){
            rows++//rows -> 15
            return
        }
        else {
            //解算
            //获取16个历元的总的加速度计数据a=sqrt(ax^2+ay^2+az^2)
            for(i in 0..rows) {
                accl[i] = sqrt(
                    sensorDataArray[i][0] * sensorDataArray[i][0]
                            + sensorDataArray[i][1] * sensorDataArray[i][1]
                            + sensorDataArray[i][2] * sensorDataArray[i][2]
                )
                yawbefore[i]= sensorDataArray[i][9]
            }
            //对accl平滑得acclSmooth
            acclSmooth=hwaSmooth(accl, smoothWindowInterval)
            acclDataArray[rows][0]= now
            acclDataArray[rows][1]= acclSmooth[rows]
            acclChart(acclDataArray)//平滑后的加速度可视化

            //脚步探测
            step= footstepsDetection(acclSmooth)
            if(step)stepint=1
            else stepint=0
//            if(step){
//
////                for(i in 0..1){
////                    stepTime[i]=stepTime[i+1]
////                }
////                var steptime=df.format(timemillis+3600000*8)
////                stepTime[2]= steptime.toString().substring(0,2).toFloat()*60+steptime.toString().substring(3).toFloat()
//            }
            //步长估计
            //stepLength= stepLengthEstimate(stepTime,1.66F)
            //stepLength=0.75F
            stepLength= StepLength
            //航向估计
            //用安卓文档提供的航向角计算方法
            //yaw= yaw(sensorData.acceleraterData,sensorData.magnetometerData)
            //yaw=(sensorData.orientation[0]*PI/180.0).toFloat()
            //yawSmooth= windowSmooth(yawbefore,50)
            yaw=yawbefore[rows]
            //室内进行航迹推算
            //室外进行GPS定位
            if(step&& isIndoor){
                //脚步
                stepint=1
                //航迹推算
                NEk1= NEk.copyOf()
                NEk[0]+=stepLength* cos(yaw)
                NEk[1]+=stepLength* sin(yaw)
                //addPoint(NEk1, NEk)
                //绘制轨迹
                //定义一个经纬度
                var latlon= ne2blh(NEk,sensorData.postionData)
                latLngs.add(LatLng(latlon[0].toDouble(), latlon[1].toDouble()))
                //aMaplocation 全局变量
                aMaplocation!!.latitude=latlon[0].toDouble();
                aMaplocation!!.longitude=latlon[1].toDouble();
                mListener!!.onLocationChanged(aMaplocation)
                //latLngs.add(desLatLng)
                aMap!!.addPolyline(
                    PolylineOptions().addAll(latLngs).width(10f).color(Color.argb(255, 0, 0, 0))
                )
            }

            dataBinding!!.logView.text="""
                            Log: 步长：${stepLength} 航向角：${sensorData.orientation[0]* 180/PI}
                                  N:${NEk[0]} E:${NEk[1]}
                            """.trimIndent()
            dataBinding!!.logView.movementMethod = ScrollingMovementMethod.getInstance()
            //使得存储的数据是连续的
            for(i in 0..rows-1) {
                sensorDataArray[i]= sensorDataArray[i+1]
                acclDataArray[i]= acclDataArray[i+1]
            }
        }

        //文件保存
        if(!isSave) return
        //输出的数据
        //格式：
        //分 秒 加速度计X轴 Y轴 Z轴 陀螺仪X轴 Y轴 Z轴 磁力计X轴 Y轴 Z轴 姿态角Yaw Roll Pitch
        //平滑之前的总加速度计 平滑后的总加速度计输出 脚步探测
        //PDR坐标北向 东向
        fileString ="${filetime} "
        for( i in 0..8) fileString+= sensorDataArray[rows][i].toString()+" "
        for( i in 9..11) fileString+= (sensorDataArray[rows][i]* 180/ PI).toString()+" "
        fileString+= accl[rows].toString()+" "+ acclSmooth[rows].toString()+" "+stepint.toString()+" "
        fileString+= NEk[0].toString()+" "+ NEk[1].toString()+" "
        //fileString+=sensorData.orientation[0].toString()+" "+(yawSmooth[rows]*180/PI).toString()+" "
        fileString+="\n"
        writer!!.write(fileString)
        //采集到的数据量
        messageNum= messageNum+ 1
    }
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged:" + sensor.type + "->" + accuracy)
    }

    private fun calculateOrientation() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravity,
            sensorData.magnetometerData
        )
        var orientationAngles = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        orientationAngles[0]=orientationAngles[0]-((4+55F/60F)*PI/180.0).toFloat()
        sensorData.orientation=orientationAngles.copyOf()
        if (sensorData.orientation[0] < 0) sensorData.orientation[0] = (sensorData.orientation[0] +PI*2).toFloat()
        //航向角处理：整体减5度 [-5,355]
        //sensorData.orientation[0]=sensorData.orientation[0]-((4+55F/60F)*PI/180.0).toFloat()
        //if(sensorData.orientation[0] < 0)sensorData.orientation[0]=0F


        for (i in 0..2) orientationAngles[i] = (orientationAngles[i] * 180.0 / PI).toFloat()
        if (orientationAngles[0] < 0) orientationAngles[0] =
            (orientationAngles[0] + 360.0).toFloat()
        //sensorData.orientation=orientationAngles.copyOf()
        Log.d(TAG, "orie: [x=${orientationAngles[0]} y=${orientationAngles[1]} z=${orientationAngles[2]}]")
        dataBinding.Yaw.text=  "Yaw  =${orientationAngles[0]}"
        dataBinding.Roll.text= "Roll =${orientationAngles[1]}"
        dataBinding.Pitch.text="Pitch=${orientationAngles[2]}"
    }
}