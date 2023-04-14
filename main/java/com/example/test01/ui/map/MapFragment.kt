package com.example.test01.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.test01.dataNum
import com.example.test01.mapBinding
//import com.example.test01.traceBitmap
//import com.example.test01.traceChart
import java.lang.String
import kotlin.FloatArray


class MapFragment : Fragment() {

    //private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = mapBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //轨迹绘制
        //traceChart()
        val root: View = binding.root
        return root
    }



    //    fun comfir(view: View?) {
//        val numberx: EditText = findViewById(R.id.numberx) as EditText
//        val numbery: EditText = findViewById(R.id.numbery) as EditText
//        val maxy: EditText = findViewById(R.id.maxy) as EditText
//        if (!numberx.getText().toString().trim().equals("") && !numbery.getText().toString().trim()
//                .equals("") && !maxy.getText().toString().trim().equals("")
//        ) {
//            /*//判断输入框中内容是否为空的错误方法：
//            numberx.getText()!=null;
//            numberx.getText().toString()!=null;
//            numberx.getText().toString().equals("");*/
//            allX = Integer.valueOf(String.valueOf(numberx.getText()))
//            allY = Integer.valueOf(String.valueOf(numbery.getText()))
//            fully = Integer.valueOf(String.valueOf(maxy.getText()))
//            xinterval = (endx1 - startx1) / allX
//            yinterval = (starty1 - endy1) / allY
//            //rule1=(starty1-endy1)/allY;
//            //rule2=(starty1-endy1)/fully;
//            mypoint = FloatArray(allX)
//            all = 0
//            chart()
//        }
//    }
    fun paint(view: View?) {


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapBinding=null
        //_binding = null
    }
}