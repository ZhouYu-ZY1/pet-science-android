package com.zhouyu.pet_science.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.databinding.ItemHistorySearchBinding

class SearchHistoryAdapter(
    private val historyList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorySearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyword = historyList[position]
        holder.binding.historySearchText.text = keyword
        holder.binding.root.setOnClickListener {
            onItemClick(keyword)
        }
    }

    override fun getItemCount(): Int = historyList.size

    inner class ViewHolder(val binding: ItemHistorySearchBinding) : RecyclerView.ViewHolder(binding.root)
}