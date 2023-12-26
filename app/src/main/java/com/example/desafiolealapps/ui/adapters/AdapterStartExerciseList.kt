package com.example.desafiolealapps.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiolealapps.R
import com.example.desafiolealapps.R.color.verde
import com.example.desafiolealapps.data.ItemExercise
import com.example.desafiolealapps.databinding.StartTrainingAdapterBinding

class AdapterStartExerciseList(private val context: Context) :
    RecyclerView.Adapter<AdapterStartExerciseList.ListExerciseViewHolder>() {

    private var listExercise = emptyList<ItemExercise>()
    private var completedItems = mutableListOf<Boolean>()
    private var onItemClickListener: OnItemClickListener? = null

    inner class ListExerciseViewHolder(private val binding: StartTrainingAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemExercise, position: Int) {
            binding.exerciseName.text = item.exerciseName
            binding.exerciseTime.text = item.exerciseTime
            binding.exerciseRepetition.text = item.exerciseRepetition

            binding.concluded.visibility = if (completedItems[position]) View.VISIBLE else View.GONE

            binding.root.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClicked(item) ?: false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListExerciseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = StartTrainingAdapterBinding.inflate(inflater, parent, false)
        return ListExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListExerciseViewHolder, position: Int) {
        val currentExercise = listExercise[position]
        holder.bind(currentExercise, position)

        holder.itemView.setOnClickListener {
            toggleItemCompletion(position)
            onItemClickListener?.onItemClicked(currentExercise)
            checkCompletionAndReset()
        }
    }

    override fun getItemCount(): Int {
        return listExercise.size
    }

    fun setData(data: List<ItemExercise>) {
        listExercise = data
        completedItems.clear()
        for (i in listExercise.indices) {
            completedItems.add(false)
        }
        notifyDataSetChanged()
    }

    private fun toggleItemCompletion(position: Int) {
        completedItems[position] = !completedItems[position]
        notifyItemChanged(position)
    }

    private fun checkCompletionAndReset() {
        if (completedItems.all { it }) {
            showMessageAndReset(context = context)
        }
    }

    private fun showMessageAndReset(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Todos os exercícios concluídos")
        builder.setMessage("Parabéns! Você concluiu todos os exercícios.")

        builder.setPositiveButton("OK") { _, _ ->
            completedItems.fill(false)
            notifyDataSetChanged()
        }
        val dialog = builder.create()
        dialog.show()
    }

    interface OnItemClickListener {
        fun onItemClicked(exercise: ItemExercise)
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