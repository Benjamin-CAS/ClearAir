package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.SearchItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights

class FoundLocationsAdapter(private val locationsListener: OnClickItemListener) :
    RecyclerView.Adapter<FoundLocationsAdapter.FoundLocationsViewHolder>() {

    private val locationsList = ArrayList<WatchedLocationHighLights>()

    interface OnClickItemListener {
        fun onClickFoundLocation(location: WatchedLocationHighLights)
    }

    class FoundLocationsViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(location: WatchedLocationHighLights, locationsListener: OnClickItemListener) {
            binding.apply {
                searchItemTv.text = location.name
            }
            itemView.setOnClickListener { locationsListener.onClickFoundLocation(location) }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoundLocationsViewHolder {
        val binding =
            SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoundLocationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoundLocationsViewHolder, position: Int) {
        val location = locationsList[position]
        holder.bind(location, locationsListener)
    }

    fun setFoundLocationsList(locationsList: List<WatchedLocationHighLights>) {
        this.locationsList.clear()
        this.locationsList.addAll(locationsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return locationsList.size
    }
}