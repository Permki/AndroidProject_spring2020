package com.team15.skredet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.team15.skredet.R
import com.team15.skredet.SharedViewModel

/**
 * Simple class displaying a layout telling the user that the network connection is faulty
 * @author Permki
 */
class NoWifiFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run { ViewModelProvider(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        return inflater.inflate(R.layout.fragment_nowifi, container, false)
    }
}

