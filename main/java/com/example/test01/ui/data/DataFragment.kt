package com.example.test01.ui.data


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.test01.*


class DataFragment : Fragment() {
    //debug

    //private var _binding: FragmentDataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    //private val binding get() = _binding!!
    private val binding get() = dataBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root: View = binding!!.root
        val SL = resources.getStringArray(R.array.spinner_data)
        dataBinding!!.clear.setOnClickListener{
            latLngs.clear()
            aMap!!.clear()

            //traceBitmap= Bitmap.createBitmap(1600, 2800, Bitmap.Config.ARGB_8888)
            //traceChart()
            binding!!.accltime.text="time"
            binding!!.acclX.text="AcclX="
            binding!!.acclY.text="AcclY="
            binding!!.acclZ.text="AcclZ="

            binding!!.gyrotime.text="time"
            binding!!.gyroX.text="GyroX="
            binding!!.gyroY.text="GyroY="
            binding!!.gyroZ.text="GyroZ="

            binding!!.magntime.text="time"
            binding!!.magnX.text="MagnX="
            binding!!.magnY.text="MagnY="
            binding!!.magnZ.text="MagnZ="

            binding!!.Yaw.text  ="Yaw"
            binding!!.Roll.text ="Roll"
            binding!!.Pitch.text="Pitch"

            binding!!.Lat.text="Lat"
            binding!!.Lon.text="Lon"
            binding!!.H.text  ="H"

            binding!!.logView.text="""
                """.trimIndent()

            NEk[0]=0.0F
            NEk[1]=0.0F

        }
        dataBinding!!.stepLength.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long) {
                StepLength=SL[position].toFloat()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }


        return root
    }





    override fun onDestroyView() {
        super.onDestroyView()
        dataBinding=null
        //_binding = null
    }

}