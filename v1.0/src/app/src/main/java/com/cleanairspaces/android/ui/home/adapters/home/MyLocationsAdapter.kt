package com.cleanairspaces.android.ui.home.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.MyLocationMapOverlayItemBinding
import com.cleanairspaces.android.models.entities.CustomerDeviceDataDetailed
import com.cleanairspaces.android.utils.AQI
import com.cleanairspaces.android.utils.MyColorUtils
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.UIColor
import java.lang.Exception

class MyLocationsAdapter(
    private val actionsListener: MyLocationsClickListener,
    private val selectedAqiIndex: String?
) :
    RecyclerView.Adapter<MyLocationsAdapter.MyLocationsViewHolder>() {

    private val myLocationsList = ArrayList<CustomerDeviceDataDetailed>()

    interface MyLocationsClickListener {
        fun onClickLocation(locationDetails: CustomerDeviceDataDetailed)
    }

    class MyLocationsViewHolder(private val binding: MyLocationMapOverlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            dataDetailed: CustomerDeviceDataDetailed,
            actionsListener: MyLocationsClickListener,
            selectedAqiIndex: String?
        ) {
            val myLocationDetails = dataDetailed.locationDetails
            val location = dataDetailed.deviceData


                binding.apply {
                    val ctx = itemView.context
                    val aqiIndex: String =
                        selectedAqiIndex ?: ctx.getString(R.string.default_pm_index_value)
                    locationNameTv.text = location.company
                    val locationArea =
                        ctx.getString(R.string.outdoor_txt) + ": " + location.location
                    locationAreaTv.text = locationArea

                    outdoorPmTv.text = aqiIndex
                    indoorPmTv.text = aqiIndex

                    val outDoorPm = myLocationDetails.outdoor.outdoor_pm.toDouble()
                    val (statusIndicatorRes, statusText, pmValue) =
                        if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
                            Triple(
                                MyColorUtils.convertUIColorToStatusRes(
                                    AQI.getAQIStatusColorFromPM25(
                                        outDoorPm
                                    )
                                ),
                                AQI.getAQIStatusTextFromPM25(outDoorPm),
                                outDoorPm.toString()
                            )
                        } else {
                            Triple(
                                MyColorUtils.convertUIColorToStatusRes(
                                    AQI.getAQICNStatusColorFromPM25(
                                        outDoorPm
                                    )
                                ),
                                AQI.getAQICNStatusTextFromPM25(outDoorPm),
                                AQI.getAQICNFromPM25(outDoorPm).toString()
                            )
                        }
                    outdoorPointsTv.text = pmValue
                    outdoorStatusIndicatorTv.text =
                        ctx.getString(statusText.conditionStrRes) //todo? use comment!
                    outdoorStatusIndicatorIv.setImageResource(statusIndicatorRes)


                    val inDoorPm = myLocationDetails.indoor.indoor_pm.toDouble()
                    val (inStatusIndicatorRes, inStatusText, inPmValue) =
                        if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
                            Triple(
                                MyColorUtils.convertUIColorToStatusRes(
                                    AQI.getAQIStatusColorFromPM25(
                                        inDoorPm
                                    )
                                ),
                                AQI.getAQIStatusTextFromPM25(inDoorPm),
                                inDoorPm.toString()
                            )
                        } else {
                            Triple(
                                MyColorUtils.convertUIColorToStatusRes(
                                    AQI.getAQICNStatusColorFromPM25(
                                        inDoorPm
                                    )
                                ),
                                AQI.getAQICNStatusTextFromPM25(inDoorPm),
                                AQI.getAQICNFromPM25(inDoorPm).toString()
                            )
                        }
                    indoorPointsTv.text = inPmValue
                    indoorStatusIndicatorTv.text =
                        ctx.getString(inStatusText.conditionStrRes) //todo? use comment!
                    indoorStatusIndicatorIv.setImageResource(inStatusIndicatorRes)

                    val bgColor = if (aqiIndex == "PM2.5" || aqiIndex == "AQI US") {
                        when {
                            AQI.getAQIFromPM25(inDoorPm) < 100 -> R.color.dark_green

                            AQI.getAQIFromPM25(inDoorPm) > 150 -> R.color.red
                            else -> R.color.orange
                        }

                    } else {
                        when {
                            AQI.getAQICNFromPM25(inDoorPm) < 150 -> R.color.dark_green
                            AQI.getAQICNFromPM25(inDoorPm) > 200 -> R.color.red
                            else -> R.color.orange
                        }
                    }
                    itemCard.setCardBackgroundColor(ContextCompat.getColor(ctx, bgColor))

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

    fun setMyLocationsList(myLocationsList: List<CustomerDeviceDataDetailed>) {
        this.myLocationsList.clear()
        this.myLocationsList.addAll(myLocationsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return myLocationsList.size
    }
}