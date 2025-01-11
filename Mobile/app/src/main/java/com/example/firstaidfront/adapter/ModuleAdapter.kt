package com.example.firstaidfront.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.firstaidfront.databinding.ModuleItemBinding
import com.example.firstaidfront.models.Module

class ModuleAdapter : RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    private var modules = listOf<Module>()

    fun submitList(newModules: List<Module>) {
        modules = newModules
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ModuleItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(modules[position], position + 1)
    }

    override fun getItemCount() = modules.size

    inner class ModuleViewHolder(private val binding: ModuleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(module: Module, position: Int) {
            binding.apply {
                moduleNumber.text = position.toString()
                moduleTitle.text = module.title
                moduleDescription.text = module.description

                // Add click listener to navigate to ModuleContentActivity
                root.setOnClickListener {
//                    val intent = Intent(itemView.context, ModuleContentActivity::class.java).apply {
//                        putExtra("module_id", module.id)
//                        putExtra("module_name", module.title)
//                    }
//                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}