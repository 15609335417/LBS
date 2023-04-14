package com.example.test01

import android.Manifest
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.LocationSource
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.UiSettings
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.PolylineOptions
import com.example.test01.databinding.ActivityMainBinding
import com.example.test01.databinding.FragmentDataBinding
import com.example.test01.databinding.FragmentMapBinding
import com.example.test01.databinding.FragmentMyBinding
import com.example.test01.ui.data.DataFragment
import com.example.test01.ui.map.MapFragment
import com.example.test01.ui.my.MyFragment
import pub.devrel.easypermissions.EasyPermissions
import java.io.*


var dataBinding:FragmentDataBinding?=null
var mapBinding:FragmentMapBinding?=null
var myBinding:FragmentMyBinding?=null

var isSensor=true
var isIndoor=false

//需要两个时间，保证同一时刻只有一个值
var now=0F
var last=0F
//文件保存
var isSave=false
const val fileName="sensordata.txt"
var fileString=""//文件内容
var messageNum=0//文件数据量
var output: OutputStream?=null //输出流
var writer: BufferedWriter?=null//缓冲区
//数组保存
const val dataNum:Int=50//数组数据量
const val smoothWindowInterval=16//平滑窗口历元数
var rows:Int=0
var sensorDataArray=Array(dataNum){FloatArray(15)}
var acclDataArray=Array(dataNum){FloatArray(2)}
var NEk=FloatArray(2)
var StepLength=0.75F

//地图上绘制轨迹
//位置更改监听
var mListener: OnLocationChangedListener? = null
var aMaplocation:AMapLocation?=null
//地图控制器
var aMap: AMap? = null
//绘制线
val latLngs: MutableList<LatLng> = ArrayList()

class MainActivity : AppCompatActivity(), LocationSource, AMapLocationListener,
    AMap.OnMapClickListener, AMap.OnMapLongClickListener {

    companion object{
        //实例化Fragment
        private val data=DataFragment()
        private val map=MapFragment()
        private val my=MyFragment()
        val list= arrayListOf(data,map,my)
        //构建FragmentTransaction来操作
        var mFragment=0//当前Fragment
        var toFragment=0//跳转页面
        private lateinit var transaction: FragmentTransaction

    }

    //请求权限码
    private val REQUEST_PERMISSIONS = 9527

    var sensorData=SensorData()
    private lateinit var binding: ActivityMainBinding
    //var dataBinding:FragmentDataBinding?=null
    private val TAG="Sensor Log:"
    //传感器
    private var sensorManager: SensorManager?=null
    private var mMySensorEventListener:MySensorEventListener?=null

    //定位
    //声明AMapLocationClient类对象
    var mLocationClient: AMapLocationClient? = null
    //声明AMapLocationClientOption对象
    var mLocationOption: AMapLocationClientOption? = null


    //位置更改监听
    //private var mListener: OnLocationChangedListener? = null
    //定位样式
    private val myLocationStyle = MyLocationStyle()

    //定义一个UiSettings对象
    private var mUiSettings: UiSettings? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        dataBinding= FragmentDataBinding.inflate(layoutInflater,null,false)
        mapBinding= FragmentMapBinding.inflate(layoutInflater,null,false)
        myBinding= FragmentMyBinding.inflate(layoutInflater,null,false)
        setContentView(binding.root)
        init()
        click()
        //传感器
        sensorManager=getSystemService(SENSOR_SERVICE) as SensorManager
        mMySensorEventListener= MySensorEventListener(this,sensorData, dataBinding!!)

        //传感器监听
        dataBinding!!.sensorstartorpause.setOnCheckedChangeListener { _, isChecked ->
            isSensor = isChecked
            if(isChecked){
                //传感器
                //加速度计
                if (sensorManager == null) return@setOnCheckedChangeListener
                val accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                if (accelerometerSensor != null){
                    //传感器存在
                    sensorManager!!.registerListener(
                        mMySensorEventListener,
                        accelerometerSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }else {
                    Log.d(TAG, "Accelerometer sensors are not supported on current devices.")
                }
                val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                if (magneticSensor != null) {
                    //register magnetic sensor listener
                    sensorManager!!.registerListener(
                        mMySensorEventListener,
                        magneticSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                } else {
                    Log.d(TAG, "Magnetic sensors are not supported on current devices.")
                }
                val gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                if (gyroscopeSensor != null) {
                    //register gyroscope sensor listener
                    sensorManager!!.registerListener(
                        mMySensorEventListener,
                        gyroscopeSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                } else {
                    Log.d(TAG, "Gyroscope sensors are not supported on current devices.")
                }
                val gravitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
                if (gravitySensor != null) {
                    //register gyroscope sensor listener
                    sensorManager!!.registerListener(
                        mMySensorEventListener,
                        gravitySensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                } else {
                    Log.d(TAG, "Gravity sensors are not supported on current devices.")
                }
            }
            else{
                if (sensorManager == null) return@setOnCheckedChangeListener
                sensorManager!!.unregisterListener(mMySensorEventListener)
            }
        }
        //室内室外：打开：室内，关闭：室外
        dataBinding!!.locationstartorpause.setOnCheckedChangeListener{_,isChecked ->
            isIndoor=isChecked
            if(isChecked){
                mLocationClient!!.stopLocation()
                latLngs.clear()
                NEk[0]=0.0F
                NEk[1]=0.0F

            }
            else{
                mLocationClient!!.startLocation()
                latLngs.clear()
            }
        }
        //保存文件选项监听
        dataBinding!!.collectdata.setOnCheckedChangeListener { _, isChecked ->
            isSave = isChecked
            if (isChecked) {
                output = openFileOutput(fileName, MODE_PRIVATE)
                writer = BufferedWriter(OutputStreamWriter(output))
                writer.use {
                    it!!.write("")
                }//s实现文件覆盖
                output=openFileOutput(fileName, MODE_APPEND)//实现文件逐行写入
                writer = BufferedWriter(OutputStreamWriter(output))
            } else {
                writer.use {
                    it!!.write("")
                }

                fileString = ""
                messageNum = 0
                //rows = 0
            }
        }

        //检查android版本，动态获取权限
        checkingAndroidVersion()
        //初始化定位
        initLocation()
        //初始化地图
        initMap(savedInstanceState)






    }

    /**
     * 检查Android版本
     */
    private fun checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android6.0及以上先获取权限再定位
            requestPermission()
        } else {
            //Android6.0以下直接定位
            //mLocationClient!!.startLocation()
        }
    }

    //页面初始化
    private fun init(){
        transaction=supportFragmentManager.beginTransaction()
        transaction.add(R.id.nav_host_fragment_activity_main,data).show(data).commit()
    }
    //页面转换
    private fun switchFragment(transaction: FragmentTransaction){
        val from=list[mFragment]
        val to=list[toFragment]
        if(mFragment== toFragment)return
        if(from.isAdded){
            if(to.isAdded){
                transaction.hide(from).show(to).commit()
            }
            else{
                transaction.add(R.id.nav_host_fragment_activity_main,to).hide(from).show(to).commit()
            }
        }else{
            if(to.isAdded){
                transaction.add(R.id.nav_host_fragment_activity_main,from).hide(from).show(to).commit()
            }
            else {
                transaction.add(R.id.nav_host_fragment_activity_main, from)
                    .add(R.id.nav_host_fragment_activity_main, to).hide(from).show(to).commit()
            }

        }
    }
    //菜单按钮点击事件
    private fun click(){
        binding.navView.setOnItemSelectedListener{
            transaction = supportFragmentManager.beginTransaction()
            when(it.itemId) {
                R.id.navigation_data -> {
                    toFragment = 0
                    switchFragment(transaction)
                    mFragment = 0
                }
                R.id.navigation_map -> {
                    toFragment = 1
                    switchFragment(transaction)
                    mFragment = 1
                }
                R.id.navigation_my -> {
                    toFragment = 2
                    switchFragment(transaction)
                    mFragment = 2
                }
            }
            true
        }
    }

    /**
     * 初始化定位
     */
    private fun initLocation() {
        //初始化定位
        try {
            mLocationClient = AMapLocationClient(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mLocationClient != null) {
            //设置定位回调监听
            mLocationClient!!.setLocationListener(this)
            //初始化AMapLocationClientOption对象
            mLocationOption = AMapLocationClientOption()
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption!!.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption!!.isOnceLocationLatest = true
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption!!.isNeedAddress = true
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption!!.httpTimeOut = 20000
            //关闭缓存机制，高精度定位会产生缓存。
            mLocationOption!!.isLocationCacheEnable = false
            //给定位客户端对象设置定位参数
            mLocationClient!!.setLocationOption(mLocationOption)
        }
    }

    /**
     * 初始化地图
     * @param savedInstanceState
     */
    private fun initMap(savedInstanceState: Bundle?) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapBinding!!.mapView.onCreate(savedInstanceState)
        //初始化地图控制器对象
        aMap = mapBinding!!.mapView.map
        //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
        aMap!!.minZoomLevel = 16F
//        // 自定义定位蓝点图标
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.point));
//        // 自定义精度范围的圆形边框颜色  都为0则透明
//        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
//        // 自定义精度范围的圆形边框宽度  0 无宽度
//        myLocationStyle.strokeWidth(0F);
//        // 设置圆形的填充颜色  都为0则透明
//        myLocationStyle.radiusFillColor(Color.argb(10, 100, 0, 0));
//        //设置定位蓝点的Style
//        aMap!!.setMyLocationStyle(myLocationStyle);

        //开启室内地图
        aMap!!.showIndoorMap(true)
        // 设置定位监听
        aMap!!.setLocationSource(this)
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.isMyLocationEnabled = true
        //实例化UiSettings类对象
        mUiSettings = aMap!!.uiSettings
        //隐藏缩放按钮
        mUiSettings!!.isZoomControlsEnabled = false
        //显示比例尺 默认不显示
        mUiSettings!!.isScaleControlsEnabled = true;
        //设置地图点击事件
        aMap!!.setOnMapClickListener(this)
        //设置地图长按事件
        aMap!!.setOnMapLongClickListener(this)


    }

    /**
     * 动态请求权限
     */
    //@AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            //true 有权限 开始定位
            showMsg("已获得权限，可以定位啦！")
            //mLocationClient!!.startLocation()
        } else {
            //false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, *permissions)
        }
    }

    /**
     * 请求权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    /**
     * Toast提示
     * @param msg 提示内容
     */
    private fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        super.onResume()
        if(!isIndoor) mLocationClient!!.startLocation()
        mapBinding!!.mapView.onResume()
        //传感器
        //加速度计
        if (sensorManager == null) return
        val accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor != null){
            //传感器存在
            sensorManager!!.registerListener(
                mMySensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }else {
            Log.d(TAG, "Accelerometer sensors are not supported on current devices.")
        }
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticSensor != null) {
            //register magnetic sensor listener
            sensorManager!!.registerListener(
                mMySensorEventListener,
                magneticSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            Log.d(TAG, "Magnetic sensors are not supported on current devices.")
        }
        val gyroscopeSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyroscopeSensor != null) {
            //register gyroscope sensor listener
            sensorManager!!.registerListener(
                mMySensorEventListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            Log.d(TAG, "Gyroscope sensors are not supported on current devices.")
        }
        val gravitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if (gravitySensor != null) {
            //register gyroscope sensor listener
            sensorManager!!.registerListener(
                mMySensorEventListener,
                gravitySensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            Log.d(TAG, "Gravity sensors are not supported on current devices.")
        }

    }

    override fun onPause() {
        super.onPause()
        mapBinding!!.mapView.onPause()
        mLocationClient!!.stopLocation()
        if (sensorManager == null) return
        sensorManager!!.unregisterListener(mMySensorEventListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapBinding!!.mapView.onSaveInstanceState(outState)
    }

    /**
     * 接收异步返回的定位结果
     *
     * @param aMapLocation
     */
    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation != null) {
            aMaplocation=aMapLocation
            if (aMapLocation.errorCode == 0) {
                //地址
                sensorData.postionData[0]=aMapLocation.latitude.toFloat()
                sensorData.postionData[1]=aMapLocation.longitude.toFloat()
                sensorData.postionData[2]=aMapLocation.altitude.toFloat()

                dataBinding!!.Lat.text="纬度：${aMapLocation.latitude}"
                dataBinding!!.Lon.text="经度：${aMapLocation.longitude}"
                dataBinding!!.H.text="海拔：${aMapLocation.altitude}"
                Log.d("MainActivity", dataBinding!!.Lat.text.toString()+" "+ dataBinding!!.Lon.text.toString())

                //停止定位后，本地定位服务不会被销毁
                //mLocationClient!!.stopLocation()
                //测试
                //aMapLocation.latitude=30.00;
                //显示地图定位结果
                if(mListener!=null){
                    //显示系统图标
                    mListener!!.onLocationChanged(aMapLocation)
                }


                if(!isIndoor){
                    //室外轨迹绘制
                    latLngs.add(LatLng(aMapLocation.latitude, aMapLocation.longitude))
                    //latLngs.add(desLatLng)
                    aMap!!.addPolyline(
                        PolylineOptions().addAll(latLngs).width(10f).color(Color.argb(255, 255, 0, 0))
                    )
                }
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e(
                    "AmapError", "location Error, ErrCode:"
                            + aMapLocation.errorCode + ", errInfo:"
                            + aMapLocation.errorInfo
                )
            }
        }
    }

    /**
     * 激活定位
     */
    override fun activate(onLocationChangedListener: OnLocationChangedListener) {
        mListener = onLocationChangedListener
        if (mLocationClient == null) {
            mLocationClient!!.startLocation() //启动定位
        }
    }

    /**
     * 停止定位
     */
    override fun deactivate() {
        mListener = null
        if (mLocationClient != null) {
            mLocationClient!!.stopLocation()
            mLocationClient!!.onDestroy()
        }
        mLocationClient = null
    }

    /**
     * 地图单击事件
     * @param latLng
     */
    override fun onMapClick(latLng: LatLng) {
        showMsg("经度：" + latLng.longitude.toFloat() + "\n 纬度：" + latLng.latitude.toFloat())
    }

    /**
     * 地图长按事件
     * @param latLng
     */
    override fun onMapLongClick(latLng: LatLng) {
        showMsg("经度：" + latLng.longitude.toFloat() + "\n 纬度：" + latLng.latitude.toFloat())
    }


    override fun onDestroy() {
        super.onDestroy()
        //销毁定位客户端，同时销毁本地定位服务。
        mLocationClient!!.onDestroy()
        mapBinding!!.mapView.onDestroy()
    }

    //文件操作
    fun save(filename:String, inputText: String) = try {
        var outputfile:OutputStream?=null
        outputfile=openFileOutput(filename, MODE_PRIVATE)
        val writer= BufferedWriter(OutputStreamWriter(outputfile))
        //这里使用了kotlin的内置函数use，它会保证在Lambda
        //表达式中的代码全部执行完之后自动将外层的流关闭，这
        //样就不再需要我们写一个finally语句，手动关闭流。
        writer.use {
            it.write(inputText)
        }
        //Toast.makeText(this,inputText,Toast.LENGTH_SHORT).show()
    }catch (e: IOException){
        e.printStackTrace()
    }

    fun saveAdd(filename:String, inputText: String) = try {
        var outputfile:OutputStream?=null
        outputfile=openFileOutput(filename, MODE_APPEND)
        val writer= BufferedWriter(OutputStreamWriter(outputfile))
        //这里使用了kotlin的内置函数use，它会保证在Lambda
        //表达式中的代码全部执行完之后自动将外层的流关闭，这
        //样就不再需要我们写一个finally语句，手动关闭流。
        writer.use {
            it.write(inputText)
        }
        //Toast.makeText(this,inputText,Toast.LENGTH_SHORT).show()
    }catch (e: IOException){
        e.printStackTrace()
    }

    fun load(name: String):String{
        val content=StringBuilder()
        try {
            val input=openFileInput(name)
            val reader= BufferedReader(InputStreamReader(input))
            //kotlin提供的内置扩展函数forEachLine，它会将读到的内容都回调到Lambda表达式中。
            reader.use {
                reader.forEachLine {
                    content.append(it+'\n')
                }
            }

        }catch(e: IOException){
            e.printStackTrace()
        }
        return content.toString()
    }

}

