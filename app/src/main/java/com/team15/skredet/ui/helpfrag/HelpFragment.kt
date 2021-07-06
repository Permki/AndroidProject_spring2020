package com.team15.skredet.ui.helpfrag

import InfoFrag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.team15.skredet.R
import com.team15.skredet.SharedViewModel
import kotlinx.android.synthetic.main.fragment_help.*

/**
 * Organizes the fragments used to display tutorial info.
 * @author Permki
 */
class HelpFragment(private var viewModel: SharedViewModel) : Fragment() {

    companion object {
        const val NUM_PAGES = 9
    }

    private lateinit var mPager: ViewPager
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    /**
     * Creates all fragments and puts them in a list used by the adapter instantiated in the same function
     * @author Permki
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPager = pager
        pagerAdapter = fragmentManager?.let { ScreenSlidePagerAdapter(it) }!!
        mPager.adapter = pagerAdapter
    }

    /**
     * A simple pager adapter that represents ScreenSlidePageFragment objects, in
     * sequence.
     * @author Permki
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = NUM_PAGES
        override fun getItem(position: Int): Fragment = InfoFrag(position, viewModel)
    }

}

