package com.cleanairspaces.android.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.databinding.MapActionsItemBinding
import com.cleanairspaces.android.ui.home.MapActionChoices
import com.cleanairspaces.android.ui.home.MapActions


class MapActionsAdapter(private val actionsListener: ClickListener) :
    RecyclerView.Adapter<MapActionsAdapter.MapActionsViewHolder>() {

    private val mapActionsList = ArrayList<MapActions>()

    interface ClickListener {
        fun onClickAction(actionChoice: MapActionChoices)
    }

    class MapActionsViewHolder(private val binding: MapActionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(action: MapActions, actionsListener: ClickListener) {
            binding.apply {
                actionTv.setText(action.action.strRes)
                actionTv.setOnClickListener {
                    actionsListener.onClickAction(action.action)
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MapActionsViewHolder {
        val binding =
            MapActionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapActionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapActionsViewHolder, position: Int) {
        val action = mapActionsList[position]
        holder.bind(action, actionsListener)
    }

    fun setMapActionsList(actionsList: List<MapActions>) {
        this.mapActionsList.clear()
        this.mapActionsList.addAll(actionsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mapActionsList.size
    }
}
