package com.cleanairspaces.android.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.MyLocationMapOverlayItemBinding
import com.cleanairspaces.android.models.entities.LocationDetailsGeneralDataWrapper
import com.cleanairspaces.android.utils.MyLocationDetailsWrapper
import com.cleanairspaces.android.utils.getLocationInfoDetails

class MyLocationsAdapter(
    private val actionsListener: MyLocationsClickListener,
) : RecyclerView.Adapter<MyLocationsAdapter.MyLocationsViewHolder>() {

    private val myLocationsList = ArrayList<LocationDetailsGeneralDataWrapper>()

    private var selectedAqiIndex: String? = null

    interface MyLocationsClickListener {
        fun onClickLocation(myLocationDetails: MyLocationDetailsWrapper)
    }

    class MyLocationsViewHolder(private val binding: MyLocationMapOverlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            dataWrapper: LocationDetailsGeneralDataWrapper,
            actionsListener: MyLocationsClickListener,
            selectedAqiIndex: String?
        ) {

            binding.apply {
                val ctx = itemView.context
                val displayInfo = getLocationInfoDetails(
                    ctx = ctx,
                    dataWrapper = dataWrapper,
                    selectedAqiIndex = selectedAqiIndex
                )
                val location = displayInfo.wrappedData.generalDataFromQr
                locationNameTv.text = location.company
                locationAreaTv.text = displayInfo.locationArea
                outdoorPmTv.text = displayInfo.aqiIndex
                indoorPmTv.text = displayInfo.aqiIndex
                outdoorPointsTv.text = displayInfo.outPmValueTxt
                outdoorStatusIndicatorTv.text = displayInfo.outStatusTvTxt //todo? use comment!
                outdoorStatusIndicatorIv.setImageResource(displayInfo.outStatusIndicatorRes)
                indoorPointsTv.text = displayInfo.inPmValueTxt
                indoorStatusIndicatorTv.text = displayInfo.inStatusTvTxt//todo? use comment!
                indoorStatusIndicatorIv.setImageResource(displayInfo.inStatusIndicatorRes)
                itemCard.setCardBackgroundColor(ContextCompat.getColor(ctx, displayInfo.bgColor))

                Glide.with(ctx)
                    .load(location.getFullLogoUrl())
                    .error(R.drawable.clean_air_spaces_logo_name)
                    .into(locationLogoIv)
                updatedTv.text = displayInfo.updatedOnTxt


                itemCard.setOnClickListener {
                    actionsListener.onClickLocation(displayInfo)
                }


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
        holder.bind(action, actionsListener, selectedAqiIndex)
    }

    fun setMyLocationsList(myLocationsList: List<LocationDetailsGeneralDataWrapper>) {
        this.myLocationsList.clear()
        this.myLocationsList.addAll(myLocationsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return myLocationsList.size
    }

    fun setAQIIndex(newAQIIndex: String?) {
        selectedAqiIndex = newAQIIndex
        notifyDataSetChanged()
    }

}