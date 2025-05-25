package com.mirackalyoncu.orvion.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mirackalyoncu.orvion.databinding.ItemMailBinding
import com.mirackalyoncu.orvion.model.Mail
import com.mirackalyoncu.orvion.roomdb.KullaniciDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MailAdapter(
    var mailList: List<Mail>,
    private val gidenKutusuMu: Boolean,
    private val onMailClick: (Mail) -> Unit,
    private val kullaniciDao: KullaniciDao
) : RecyclerView.Adapter<MailAdapter.MailViewHolder>() {

    private val compositeDisposable = CompositeDisposable()

    inner class MailViewHolder(val binding: ItemMailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MailViewHolder {
        val binding = ItemMailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MailViewHolder, position: Int) {
        val mail = mailList[position]

        holder.binding.apply {
            textViewKonu.text = mail.konu
            textViewIcerik.text = mail.icerik
            textViewTarih.text = mail.tarih


            val ilgiliKullaniciId = if (gidenKutusuMu) mail.aliciId else mail.gonderenId
            val etiket = if (gidenKutusuMu) "Alıcı" else "Gönderen"

            val disposable = kullaniciDao.getKullaniciById(ilgiliKullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ kullanici ->
                    textViewAlici.text = "$etiket: ${kullanici.email}"
                    textViewAlici.visibility = View.VISIBLE
                }, {
                    textViewAlici.text = "$etiket: Bilinmiyor"
                    textViewAlici.visibility = View.VISIBLE
                })

            compositeDisposable.add(disposable)


            if (!gidenKutusuMu) {
                if (mail.okundu) {
                    imageViewOkunduIcon.setImageResource(com.mirackalyoncu.orvion.R.drawable.read_mail)
                    viewOkunmamisGosterge.visibility = View.GONE
                } else {
                    imageViewOkunduIcon.setImageResource(com.mirackalyoncu.orvion.R.drawable.unread_mail)
                    viewOkunmamisGosterge.visibility = View.VISIBLE
                }
            } else {
                imageViewOkunduIcon.visibility = View.GONE
                viewOkunmamisGosterge.visibility = View.GONE
            }


            root.setOnClickListener {

                onMailClick(mail)
            }
        }
    }

    override fun getItemCount(): Int = mailList.size

    fun clearDisposables() {
        compositeDisposable.clear()
    }
}
