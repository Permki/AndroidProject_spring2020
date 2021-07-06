package com.team15.skredet

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


/**
 * This class distributes a lot of functions globally. Both extension functions and ordinary functions
 * With this class we get a much more clean and comprehensive code throughout.
 * @author Permki
 */


/**
 * Extension to calendar.
 * Creates a formatted datestring to suit API-requests.
 * @author Permki
 * @return Formatted datestring on format "YYYY-MM-DD"
 */
fun GregorianCalendar.createFormattedString(): String {
    return this.get(GregorianCalendar.YEAR)
        .toString() + "-" + (this.get(GregorianCalendar.MONTH) + 1) + "-" + this.get(
        GregorianCalendar.DAY_OF_MONTH
    )
}

/**
 * Extension to calendar.
 * Creates a formatted datestring with time of day
 * @author Permki
 * @return Formatted datestring on format "YYYY-MM-DD kl HH:MM"
 */
fun GregorianCalendar.createEnhancedFormattedString(): String {
    return this.get(GregorianCalendar.YEAR)
        .toString() + "-" + (this.get(GregorianCalendar.MONTH) + 1) + "-" + this.get(
        GregorianCalendar.DAY_OF_MONTH
    ) + " kl " + this.get(GregorianCalendar.HOUR_OF_DAY) + ":" + this.get(
        GregorianCalendar.MINUTE
    )
}

/**
 * Extension to String. Creates toast with string.
 * @author Permki
 */
fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}


/**
 * Gives the date +- i on format "YYYY-MM-DD"
 * @author Permki
 * @param i 0 is today, 1 is tomorrow, -1 is yesterday....
 * @return String on format "YYYY-MM-DD"
 */
fun getDate(i: Int): String {
    val cal = GregorianCalendar()
    cal.add(GregorianCalendar.DAY_OF_YEAR, i)
    return cal.createFormattedString()
}

/**
 * Extension to Views, showing or hiding them
 * @author Permki
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

/**
 * Extension to Views, sliding the views to the right or left
 * @author Permki
 */
fun View.slideLeft() {
    this.animate().translationX(-this.width.toFloat())
}

fun View.slideRight() {
    this.animate().translationX(this.width.toFloat())
}

/**
 * Extension to ImageButtons, setting or removing color
 * @author Permki
 */
fun ImageButton.colorBlack() {
    this.setColorFilter(0xE81626, PorterDuff.Mode.SRC_ATOP)
}

fun ImageButton.removeColor() {
    this.colorFilter = null
}

/**
 * Extension to View, handling keyboard being visible or not
 * @author Permki
 */
fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Extension to GoogleMap for setting markers.
 * @author Permki
 */
fun GoogleMap.placeMarkerOnMap(location: LatLng): Marker {
    return this.addMarker(MarkerOptions().position(location))
}

/**
 * Displays a row of dots at bottom of layout. One of which marks the current by filling it
 * @author Permki
 * @param cont -> Context of sender
 * @param layout_dots -> Linear layout used for displaying dots at bottom of layout
 * @param current -> the index of the layout to be drawn to
 * @param num_pages -> the number of pages in total
 */
fun addBottomDots(cont: Context, layout_dots: LinearLayout, current: Int, num_pages: Int) {
    val dots: Array<ImageView?> = arrayOfNulls(num_pages)
    layout_dots.removeAllViews()
    for (i in dots.indices) {
        dots[i] = ImageView(cont)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(15, 15))
        params.setMargins(10, 10, 10, 10)
        dots[i]?.layoutParams = params
        dots[i]?.setImageResource(R.drawable.shape_circle_outline)
        layout_dots.addView(dots[i])
    }
    if (dots.isNotEmpty()) {
        dots[current]?.setImageResource(R.drawable.shape_circle)
    }
}