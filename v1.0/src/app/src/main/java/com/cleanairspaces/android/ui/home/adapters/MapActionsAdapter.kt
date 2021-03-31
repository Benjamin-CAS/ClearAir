package com.cleanairspaces.android.ui.home.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.databinding.MapActionsItemBinding
import com.cleanairspaces.android.ui.home.MapActionChoices
import com.cleanairspaces.android.ui.home.MapActions
import com.cleanairspaces.android.ui.home.gmap.GMapActivity
import kotlinx.parcelize.Parcelize


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
                actionBtn.setText(action.action.strRes)
                actionBtn.setOnClickListener {
                    actionsListener.onClickAction(action.action)
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MapActionsAdapter.MapActionsViewHolder {
        val binding =
            MapActionsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapActionsAdapter.MapActionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapActionsAdapter.MapActionsViewHolder, position: Int) {
        val action= mapActionsList[position]
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
