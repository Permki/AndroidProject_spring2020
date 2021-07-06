package com.team15.skredet.ui.region

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.team15.skredet.*
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import kotlinx.android.synthetic.main.region_list_element.view.*

private lateinit var viewModel: SharedViewModel

/**
 * Class administrating adapter for regions-frag and favorites-frag. Populates list of cardviews with relevant
 * information and initializes listeners
 * @author Permki & Sarekhs
 */
class ListAdapter(private val items: MutableList<AllRegionsDetailsData>, owner: LifecycleOwner) :
    RecyclerView.Adapter<ViewHolder>() {

    private var viewLifecycleOwner = owner
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Sharing viewmodel
     * @author Sarekhs
     */
    fun giveViewModel(vm: SharedViewModel) {
        viewModel = vm
    }

    /**
     * Selfexplanatory
     * @author Permki & Sarekhs
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.region_list_element, parent, false)
        )
    }

    /**
     * Function binding cardview to listrepresentation in fragment. All relevant information
     * updated and listeners specific for that card are set.
     * @author Permki & Sarekhs
     * @param holder -> ViewHolder used to display
     * @param position -> literal position in list
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val region = items[position]
        val id = region.Id
        val warning =
            region.AvalancheWarningList[viewModel.numDays.value?.minus(viewModel.futurePredictionSize)!!]
        val dangerlevel = warning.DangerLevel
        val v = holder.view
        if (dangerlevel != "0") {
            v.profilePic.show()
            v.elementNoImageText.hide()
            Glide.with(v).load(warning.AvalancheAdvices[0].ImageUrl).into(v.profilePic)
        } else {
            v.profilePic.hide()
            v.elementNoImageText.show()
        }
        viewModel.colorPicker[dangerlevel]?.let { v.element_dangerlevel.setBackgroundColor(it) }
        v.element_region_name.text = region.Name
        v.element_dangerlevel.text =
            viewModel.main.getString(R.string.dangerlevel_map_text).format(dangerlevel)
        updateFavorite(holder, id)
        v.openDetailedInfo.setOnClickListener { viewModel.openDetailFragment((id)) }
        v.more_info.setOnClickListener { viewModel.openDetailFragment((id)) }
        v.favoriteButton.setOnClickListener {
            viewModel.addToFavorites(id, dangerlevel)
            updateFavorite(holder, id)
        }
        viewModel.updateFavorites.observe(
            viewLifecycleOwner,
            Observer { updateFavorite(holder, id) })
    }

    /**
     * Colors hearticon in card according to presens in favoriteslist.
     * @author Haakose
     * @param stop  signals that all backgroundprocesses should stop if set to true, used in onStart()
     */
    private fun updateFavorite(holder: ViewHolder, id: Int) {
        if (id in viewModel.favoritesList.keys) {
            holder.view.favoriteButton.colorBlack()
        } else {
            holder.view.favoriteButton.removeColor()
        }
    }
}

class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
