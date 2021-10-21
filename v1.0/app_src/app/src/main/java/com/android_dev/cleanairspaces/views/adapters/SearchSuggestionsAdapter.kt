package com.android_dev.cleanairspaces.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.SearchItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData

class SearchSuggestionsAdapter(private val suggestionsListener: OnClickItemListener) :
    RecyclerView.Adapter<SearchSuggestionsAdapter.SearchSuggestionsViewHolder>() {

    private val suggestionsList = ArrayList<SearchSuggestionsData>()

    interface OnClickItemListener {
        fun onClickSearchSuggestion(suggestion: SearchSuggestionsData)
    }

    class SearchSuggestionsViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(suggestion: SearchSuggestionsData, suggestionsListener: OnClickItemListener) {
            binding.apply {
                searchItemTv.text = suggestion.nameToDisplay
            }
            itemView.setOnClickListener { suggestionsListener.onClickSearchSuggestion(suggestion) }
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

    fun setSearchSuggestionsList(suggestionsList: List<SearchSuggestionsData>) {
        Log.e(TAG, "setSearchSuggestionsList: $suggestionsList")
        this.suggestionsList.clear()
        this.suggestionsList.addAll(suggestionsList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return suggestionsList.size
    }
    companion object{
        const val TAG = "SearchSuggestionsAdapter"
    }
}