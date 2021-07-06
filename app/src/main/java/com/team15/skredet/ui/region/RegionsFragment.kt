package com.team15.skredet.ui.region

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.team15.skredet.R
import com.team15.skredet.SharedViewModel
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import kotlinx.android.synthetic.main.fragment_regions.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Creates the RegionFragment by collecting data and attaching the adapter
 * @author Sarekhs & Permki
 */
class RegionsFragment : Fragment() {
    private lateinit var viewModel: SharedViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run { ViewModelProvider(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        retainInstance = true
        getData()
        viewModel.update.observe(viewLifecycleOwner, Observer { getData() })
        return inflater.inflate(R.layout.fragment_regions, container, false)
    }

    /**
     * Awaits the main datacollection and creates adapter
     * @author Sarekhs & Permki
     */
    private fun getData() = MainScope().launch {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        while (viewModel.mainInfoMap.size == 0) delay(5)
        val regions = mutableListOf<AllRegionsDetailsData>()
        for (elem in viewModel.mainInfoMap.values) regions.add(elem)
        val adapter = ListAdapter(regions, viewLifecycleOwner)
        adapter.giveViewModel(viewModel)
        recyclerView.adapter = adapter
    }
}
