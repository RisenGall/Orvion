package com.mirackalyoncu.orvion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mirackalyoncu.orvion.databinding.SurecRecyclerRowBinding
import com.mirackalyoncu.orvion.model.SurecWithProgress

class SurecAdapter(private var surecList: List<SurecWithProgress>, private val itemClickListener: (SurecWithProgress) -> Unit) :
    RecyclerView.Adapter<SurecAdapter.SurecViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurecViewHolder {
        val binding = SurecRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurecViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurecViewHolder, position: Int) {
        var surec = surecList[position]
        holder.bind(surec)
    }

    override fun getItemCount(): Int = surecList.size

    inner class SurecViewHolder(private val binding: SurecRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(surec: SurecWithProgress) {
            binding.surecIsmiTextView.text = surec.surecAdi

            val oran = surec.tamamlanmaOrani
            binding.progressCircle.progress = oran
            binding.percentageText.text = "$oran%"

            itemView.setOnClickListener {
                itemClickListener(surec)
            }
        }
    }

    fun updateData(yeniListe: List<SurecWithProgress>) {
        this.surecList = yeniListe
        notifyDataSetChanged()
    }

}
