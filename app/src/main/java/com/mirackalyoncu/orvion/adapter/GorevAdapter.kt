package com.mirackalyoncu.orvion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mirackalyoncu.orvion.R
import com.mirackalyoncu.orvion.databinding.GorevRecyclerRowBinding
import com.mirackalyoncu.orvion.model.Gorev

class GorevAdapter(
    private val context: Context,
    private var gorevList: MutableList<Gorev>,
    private var surecAdlariMap: Map<Int, String>,
    private val onDurumGuncelle: (Gorev) -> Unit
) : RecyclerView.Adapter<GorevAdapter.GorevViewHolder>() {

    inner class GorevViewHolder(val binding: GorevRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GorevViewHolder {
        val binding = GorevRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GorevViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GorevViewHolder, position: Int) {
        val gorev = gorevList[position]
        val binding = holder.binding
        val surecAdi = surecAdlariMap[gorev.surecId] ?: "Bilinmeyen Süreç"





        binding.tvSurecBaslik.text = "Süreç İsmi: $surecAdi"
        binding.tvGorevBaslik.text = gorev.baslik
        binding.tvGorevAciklama.text = gorev.aciklama
        binding.tvSonTarih.text = "Bitiş Tarihi: ${gorev.sonTarih}"

        val durumlar = context.resources.getStringArray(R.array.gorev_durumlari)
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, durumlar)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDurum.adapter = spinnerAdapter

        binding.spinnerDurum.setSelection(durumIndex(gorev.durum))
        binding.spinnerDurum.isEnabled = gorev.durum != "Tamamlandı"

        binding.spinnerDurum.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                val secilenDurum = parent.getItemAtPosition(pos).toString()

                if (gorev.durum == "Tamamlandı") {
                    binding.spinnerDurum.isEnabled = false
                    return
                }

                if (gorev.durum != "Beklemede" && secilenDurum == "Beklemede") {

                    binding.spinnerDurum.setSelection(durumIndex(gorev.durum))
                    return
                }

                if (secilenDurum != gorev.durum) {
                    val updatedGorev = gorev.copy(durum = secilenDurum)
                    onDurumGuncelle(updatedGorev)

                    if (secilenDurum == "Tamamlandı") {
                        Toast.makeText(context, "Görev tamamlandı olarak işaretlendi.", Toast.LENGTH_SHORT).show()

                        val currentPosition = holder.adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            gorevList.removeAt(currentPosition)
                            notifyItemRemoved(currentPosition)

                            gorevList.add(updatedGorev)
                            notifyItemInserted(gorevList.size - 1)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun getItemCount(): Int = gorevList.size

    private fun durumIndex(durum: String): Int {
        return when (durum) {
            "Beklemede" -> 0
            "Devam Ediyor" -> 1
            "Tamamlandı" -> 2
            else -> 0
        }
    }

    fun updateList(newList: List<Gorev>) {
        gorevList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateSurecAdlariMap(yeniMap: Map<Int, String>) {
        surecAdlariMap = yeniMap
        notifyDataSetChanged()
    }
}
