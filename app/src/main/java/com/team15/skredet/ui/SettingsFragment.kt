package com.team15.skredet.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.team15.skredet.R
import com.team15.skredet.SharedViewModel
import com.team15.skredet.hideKeyboard
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * Creates settingsfragment by handling various listeners
 * @author Permki & Haakose
 */
class SettingsFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run { ViewModelProvider(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        retainInstance = true
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pushWarningUpSwitch.isChecked = viewModel.pushWarningUp
        pushWarningDownSwitch.isChecked = viewModel.pushWarningDown
        if (viewModel.colorMap.value != null) settings_color_switch.isChecked =
            viewModel.colorMap.value!!

        if (viewModel.mapType.value != null) maptypeSpinner.setSelection(viewModel.mapType.value!!)

        numDaysTextView.setText(viewModel.numDays.value.toString(), TextView.BufferType.EDITABLE)
        viewModel.tempNumDays = viewModel.numDays.value!!
        settings_color_switch.setOnCheckedChangeListener { _, _ ->
            println("setting switch == " + settings_color_switch.isChecked)
            viewModel.colorMap.value = settings_color_switch.isChecked
        }
        plusOneButton.setOnClickListener {
            if (viewModel.tempNumDays < viewModel.maxNumDays) viewModel.tempNumDays++
            numDaysTextView.setText(viewModel.tempNumDays.toString(), TextView.BufferType.EDITABLE)
        }
        minusOneButton.setOnClickListener {
            if (viewModel.tempNumDays > viewModel.futurePredictionSize) viewModel.tempNumDays--
            numDaysTextView.setText(viewModel.tempNumDays.toString(), TextView.BufferType.EDITABLE)
        }
        numDaysTextView.setOnClickListener {
            numDaysTextView.setSelectAllOnFocus(true)
        }
        save_settings.setOnClickListener { viewModel.main.onBackPressed() }
        pushWarningUpSwitch.setOnCheckedChangeListener { _, _ ->

            viewModel.pushWarningUp = pushWarningUpSwitch.isChecked
        }
        pushWarningDownSwitch.setOnCheckedChangeListener { _, _ ->
            println("setting switch == " + pushWarningDownSwitch.isChecked)
            viewModel.pushWarningDown = pushWarningDownSwitch.isChecked
        }
        numDaysTextView.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (numDaysTextView.text.isNotEmpty() && numDaysTextView.text.toString()
                        .toInt() > 2 && numDaysTextView.text.toString()
                        .toInt() <= viewModel.maxNumDays
                ) {
                    viewModel.tempNumDays = numDaysTextView.text.toString().toInt()
                }
                numDaysTextView.setText(
                    viewModel.tempNumDays.toString(),
                    TextView.BufferType.EDITABLE
                )
                numDaysTextView.hideKeyboard()
            };false
        }
        removeUserData.setOnClickListener {
            viewModel.deleteUserData.value = true
        }

    }
}

