package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.android_dev.cleanairspaces.databinding.AirConditionerItemBinding
import com.android_dev.cleanairspaces.views.adapters.view_holders.AirConditionerViewHolder

/**
 * @author Benjamin
 * @description:
 * @date :2021.10.13 18:04
 */
class AirConditionerAdapter:ListAdapter<String, AirConditionerViewHolder>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ) =
            oldItem == newItem

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ) =
            oldItem == newItem

    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AirConditionerViewHolder(
        AirConditionerItemBinding.inflate(
        LayoutInflater.from(parent.context),parent,false)).apply {

    }

    override fun onBindViewHolder(holder: AirConditionerViewHolder, position: Int) {

    }

}