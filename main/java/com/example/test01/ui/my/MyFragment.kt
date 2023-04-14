package com.example.test01.ui.my

import android.R
import android.content.Context.MODE_APPEND
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.test01.*
import java.io.BufferedWriter
import java.io.OutputStreamWriter


class MyFragment : Fragment() {


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = myBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val root: View = binding.root

        return root
        }

    override fun onDestroyView() {
        super.onDestroyView()
        myBinding=null
    }
}