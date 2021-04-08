package com.cleanairspaces.android.ui.home.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.MyLocationMapOverlayItemBinding
import com.cleanairspaces.android.models.entities.CustomerDeviceDataDetailed

class MyLocationsAdapter(private val actionsListener: MyLocationsClickListener) :
    RecyclerView.Adapter<MyLocationsAdapter.MyLocationsViewHolder>() {

    private val myLocationsList = ArrayList<CustomerDeviceDataDetailed>()

    interface MyLocationsClickListener {
        fun onClickLocation(locationDetails: CustomerDeviceDataDetailed)
    }

    class MyLocationsViewHolder(private val binding: MyLocationMapOverlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            dataDetailed: CustomerDeviceDataDetailed,
            actionsListener: MyLocationsClickListener
        ) {
            val myLocationDetails = dataDetailed.locationDetails
            val location = dataDetailed.deviceData

            binding.apply {
                val ctx = itemView.context
                locationNameTv.text = location.company
                locationAreaTv.text = location.location
                indoorPmTv.text = myLocationDetails.indoor.indoor_pm
                outdoorPmTv.text = myLocationDetails.outdoor.outdoor_pm
                indoorPointsTv.text = "" //todo
                outdoorPointsTv.text = "" //todo
                //todo indoorStatusIndicatorTv.setText("")
                //todo indoorStatusIndicatorIv.setImageResource(//statusIndicatorRes)
                //todo outdoorStatusIndicatorTv.setText("")
                //todo outdoorStatusIndicatorIv.setImageResource(//statusIndicatorRes)
                Glide.with(ctx)
                    .load(location.getFullLogoUrl())
                    .error(R.drawable.clean_air_spaces_logo_name)
                    .into(locationLogoIv)
                val updatedOn =
                    ctx.getString(R.string.updated_on_prefix) + "\n" + myLocationDetails.getFormattedUpdateTime()
                updatedTv.text = updatedOn
                itemCard.setOnClickListener {
                    actionsListener.onClickLocation(dataDetailed)
                }
                itemCard.setBackgroundColor(ContextCompat.getColor(ctx, R.color.green))
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyLocationsViewHolder {
        val binding =
            MyLocationMapOverlayItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return MyLocationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyLocationsViewHolder, position: Int) {
        val action = myLocationsList[position]
        holder.bind(action, actionsListener)
    }

    fun setMyLocationsList(myLocationsList: List<CustomerDeviceDataDetailed>) {
        this.myLocationsList.clear()
        this.myLocationsList.addAll(myLocationsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return myLocationsList.size
    }
}