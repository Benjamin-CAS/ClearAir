package com.cleanairspaces.android.ui.search_locations.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cleanairspaces.android.databinding.SearchItemBinding
import com.cleanairspaces.android.models.entities.SearchSuggestions

class SearchSuggestionsAdapter(private val suggestionsListener: OnClickItemListener ) :
    RecyclerView.Adapter<SearchSuggestionsAdapter.SearchSuggestionsViewHolder>() {

    private val suggestionsList = ArrayList<SearchSuggestions>()

    interface OnClickItemListener  {
        fun onClickAction(suggestion: SearchSuggestions)
    }

    class SearchSuggestionsViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(suggestion: SearchSuggestions, suggestionsListener: OnClickItemListener) {
            binding.apply {
                searchItemTv.text = suggestion.location_name
            }
            itemView.setOnClickListener { suggestionsListener.onClickAction(suggestion) }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuggestionsViewHolder {
        val binding =
            SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchSuggestionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchSuggestionsViewHolder, position: Int) {
        val suggestion = suggestionsList[position]
        holder.bind(suggestion, suggestionsListener)
    }

    fun setSearchSuggestionsList(suggestionsList: List<SearchSuggestions>) {
        this.suggestionsList.clear()
        this.suggestionsList.addAll(suggestionsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return suggestionsList.size
    }
}