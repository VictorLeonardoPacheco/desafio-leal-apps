package com.example.desafiolealapps.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.desafiolealapps.R
import com.example.desafiolealapps.data.ItemExercise
import com.example.desafiolealapps.databinding.ExerciseAdapterBinding

class AdapterExerciseList : RecyclerView.Adapter<AdapterExerciseList.ListExerciseViewHolder>() {

    private var listExercise = emptyList<ItemExercise>()
    private var onItemClickListener: OnItemClickListener? = null

    inner class ListExerciseViewHolder(private val binding: ExerciseAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemExercise) {
            binding.exerciseName.text = item.exerciseName
            binding.exerciseTime.text = item.exerciseTime
            binding.exerciseRepetition.text = item.exerciseRepetition


            if (!item.exerciseImageUrl.isNullOrBlank()) {
                Glide.with(binding.root)
                    .load(item.exerciseImageUrl)
                    .error(R.drawable.gym_klaus)
                    .into(binding.imageExercise)
            } else {
                binding.imageExercise.setImageResource(R.drawable.gym_klaus)
            }

            binding.root.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClicked(item) ?: false
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListExerciseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ExerciseAdapterBinding.inflate(inflater, parent, false)
        return ListExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListExerciseViewHolder, position: Int) {
        val currentExercise = listExercise[position]
        holder.bind(currentExercise)

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClicked(currentExercise)
        }
    }

    override fun getItemCount(): Int {
        return listExercise.size
    }

    fun setData(data: List<ItemExercise>) {
        listExercise = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClicked(training: ItemExercise)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    private var onItemLongClickListener: OnItemLongClickListener? = null

    interface OnItemLongClickListener {
        fun onItemLongClicked(exercise: ItemExercise): Boolean
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        onItemLongClickListener = listener
    }


}