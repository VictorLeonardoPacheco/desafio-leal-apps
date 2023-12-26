package com.example.desafiolealapps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiolealapps.R
import com.example.desafiolealapps.data.ItemTraining
import com.example.desafiolealapps.databinding.TrainingAdapterBinding

class AdapterTrainingList :RecyclerView.Adapter<AdapterTrainingList.ListTrainingViewHolder>() {

        private var listTraining = emptyList<ItemTraining>()
        private var onItemClickListener: OnItemClickListener? = null
        inner class ListTrainingViewHolder(private val binding: TrainingAdapterBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ItemTraining) {
                binding.trainingName.text = item.name
                binding.trainingDays.text = item.trainingDays
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListTrainingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TrainingAdapterBinding.inflate(inflater, parent, false)
            return ListTrainingViewHolder(binding)
        }

    override fun onBindViewHolder(holder: ListTrainingViewHolder, position: Int) {
        val currentTraining = listTraining[position]
        holder.bind(currentTraining)

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClicked(currentTraining)
        }

        holder.itemView.findViewById<ImageView>(R.id.start_training)?.setOnClickListener {
            onItemClickListener?.onStartTrainingClicked(currentTraining)
        }
    }

        override fun getItemCount(): Int {
            return listTraining.size
        }

        fun setData(data: List<ItemTraining>) {
            listTraining = data
            notifyDataSetChanged()
        }

    interface OnItemClickListener {
        fun onItemClicked(training: ItemTraining)
        fun onStartTrainingClicked(training: ItemTraining)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }


}