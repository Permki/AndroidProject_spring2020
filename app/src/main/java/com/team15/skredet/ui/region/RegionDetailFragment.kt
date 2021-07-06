package com.team15.skredet.ui.region

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import com.team15.skredet.*
import com.team15.skredet.dataclasses.CoordinateDetails
import com.team15.skredet.dataclasses.CoordinateDetailsItem
import kotlinx.android.synthetic.main.fragment_regiondetails.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Creates DetailFragment displaying all necessary information about a region
 * @author Permki
 */
class RegionDetailFragment : Fragment() {

    private var adviceIndex = 0
    private lateinit var selectedDate: GregorianCalendar;
    private lateinit var maxDate: GregorianCalendar
    private lateinit var vm: SharedViewModel
    private lateinit var element: CoordinateDetailsItem

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vm = activity?.run { ViewModelProvider(this)[SharedViewModel::class.java] }
            ?: throw Exception("Not able to attach vm to RegionDetailFragment")
        vm.updateDetails.observe(viewLifecycleOwner, Observer { enterDetailFrag() })
        return inflater.inflate(R.layout.fragment_regiondetails, container, false)
    }

    /**
     * Sets the maximal date (i.e. 2 days prediction), and activates listeners
     * @author Permki
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        vm.detailFavoriteIcon = detail_favoriteButton
        maxDate = GregorianCalendar()
        maxDate.add(GregorianCalendar.DAY_OF_YEAR, vm.futurePredictionSize - 1)
        createListeners()
        super.onActivityCreated(savedInstanceState)
    }

    /**
     * Calls on datacollection after retrieving current-date
     * @author Permki
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun enterDetailFrag() {
        selectedDate = vm.currentDate
        getSpecificDate()
    }

    /**
     * Main part of detailed fragment. Updates all views and collects data for the specified date
     * @author Permki
     */
    private fun getSpecificDate() = MainScope().launch {
        detailsProgressbar.show()
        vm.setColorOnHeart()
        dateBox.text = selectedDate.createFormattedString()
        adviceIndex = 0
        try {
            val response = Fuel.get(
                getString(R.string.by_coordinates_details_general).format(
                    vm.getLatText(),
                    vm.getLongText(),
                    dateBox.text,
                    dateBox.text
                )
            ).awaitString()
            vm.internet.value = true
            element = Gson().fromJson(response, CoordinateDetails::class.java)[0]
            dangerlevel.text = getString(R.string.dangerlevel_map_text).format(element.DangerLevel)
            vm.colorPicker[element.DangerLevel]?.let { dangerlevel.setBackgroundColor(it) }

            if (element.DangerLevel != "0") {
                elementNoImageText.hide()
                detail_picture.show()
                context?.let {
                    Glide.with(it).load(element.AvalancheAdvices[adviceIndex].ImageUrl)
                        .into(detail_picture)
                }
                ObjectAnimator.ofFloat(detail_picture, View.ALPHA, 0.2f, 1.0f).setDuration(1000)
                    .start();
                mainText.text =
                    createPimpString(getString(R.string.maintextHeader), element.MainText)
                details_advices.text = createPimpString(
                    getString(R.string.adviceHeader),
                    element.AvalancheAdvices[adviceIndex].Text
                )
            } else {
                mainText.text = createPimpString(
                    getString(R.string.maintextHeader),
                    getString(R.string.no_data_available)
                )
                details_advices.text = createPimpString(
                    getString(R.string.adviceHeader),
                    getString(R.string.no_data_available)
                )
                elementNoImageText.show()
                detail_picture.hide()
            }
        } catch (e: Exception) {
            vm.updateFailureOrigin = "Details"
            vm.internet.value = false
        }
        detailsProgressbar.hide()
    }

    /**
     * Creates enhanced headers for strings. Visually more appealing
     * @author Permki
     */
    private fun createPimpString(header: String, body: String) =
        SpannableStringBuilder().bold { append(header) }.append(body)


    /**
     * Selfexplanatory
     * @author Permki
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun createListeners() {
        detail_picture.setOnClickListener {
            adviceIndex++
            if (adviceIndex == element.AvalancheAdvices.size) adviceIndex = 0
            context?.let {
                Glide.with(it).load(element.AvalancheAdvices[adviceIndex].ImageUrl)
                    .into(detail_picture)
            }
            details_advices.text = createPimpString(
                getString(R.string.adviceHeader),
                element.AvalancheAdvices[adviceIndex].Text
            )
        }
        plusDay.setOnClickListener {
            if (selectedDate.time < maxDate.time) {
                addDay(1)
            }
        }
        minusDay.setOnClickListener { addDay(-1) }
        datePicker.setOnClickListener { showCalendar() }
        dateBox.setOnClickListener { showCalendar() }
        detail_favoriteButton.setOnClickListener { vm.addToFavorites() }
    }

    /**
     * Increases or decreases day date by one day and calls on datacollection
     * @author Permki
     */
    private fun addDay(value: Int) {
        selectedDate.add(GregorianCalendar.DAY_OF_YEAR, value)
        getSpecificDate()
    }

    /**
     * Selfexplanatory. Restricts calendar selection to max-date and retrieves user selection
     * @author Permki
     */
    private fun showCalendar() {
        val cldr: Calendar = Calendar.getInstance()
        val dpDialog = context?.let {
            DatePickerDialog(it, OnDateSetListener { _, yearOfYear, monthOfYear, dayOfMonth ->
                selectedDate = GregorianCalendar(yearOfYear, monthOfYear, dayOfMonth)
                getSpecificDate()
            }, cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH))
        }
        dpDialog?.datePicker?.maxDate = maxDate.timeInMillis
        dpDialog?.show()
    }

}