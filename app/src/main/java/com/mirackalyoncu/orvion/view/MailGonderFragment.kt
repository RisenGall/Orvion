package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mirackalyoncu.orvion.R
import com.mirackalyoncu.orvion.databinding.FragmentMailGonderBinding
import com.mirackalyoncu.orvion.model.Mail
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class MailGonderFragment : Fragment() {

    private var _binding: FragmentMailGonderBinding? = null
    private val binding get() = _binding!!
    private val disposable = CompositeDisposable()
    private val args: MailGonderFragmentArgs by navArgs()

    private var aliciKullaniciId: Int? = null
    private lateinit var kullaniciEmailList: List<String>
    private lateinit var kullaniciIdList: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMailGonderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gonderenId = args.kullaniciId


        kullanicilariYukle()


        binding.buttonGonder.setOnClickListener {
            val konu = binding.editTextKonu.text.toString().trim()
            val icerik = binding.editTextIcerik.text.toString().trim()

            if (aliciKullaniciId == null) {
                Toast.makeText(requireContext(), "Alıcı seçiniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (konu.isEmpty()) {
                Toast.makeText(requireContext(), "Konu boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (icerik.isEmpty()) {
                Toast.makeText(requireContext(), "İçerik boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tarih = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
            val mail = Mail(

                gonderenId = gonderenId,
                aliciId = aliciKullaniciId!!,
                konu = konu,
                icerik = icerik,
                tarih = tarih
            )
            mailGonder(mail)
        }

        binding.spinnerAlicilar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                aliciKullaniciId = kullaniciIdList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    private fun kullanicilariYukle() {
        val kullaniciDao = OrvionDatabase.getInstance(requireContext()).kullaniciDao()

        disposable.add(
            kullaniciDao.getAllKullanicilar()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ kullanicilar ->
                    kullaniciEmailList = kullanicilar.map { it.email }
                    kullaniciIdList = kullanicilar.map { it.kullaniciId }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        kullaniciEmailList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerAlicilar.adapter = adapter
                }, { hata ->
                    hata.printStackTrace()
                    Toast.makeText(requireContext(), "Kullanıcılar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                })
        )
    }

    private fun mailGonder(mail: Mail) {
        val mailDao = OrvionDatabase.getInstance(requireContext()).mailDao()

        disposable.add(
            mailDao.mesajGonder(mail)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(requireContext(), "Mail gönderildi", Toast.LENGTH_SHORT).show()
                    val action = MailGonderFragmentDirections
                        .actionMailGonderFragmentToGidenKutusuFragment(kullaniciId = mail.gonderenId)
                    findNavController().navigate(action)

                }, { hata ->
                    hata.printStackTrace()
                    Toast.makeText(requireContext(), "Mail gönderilirken hata oluştu", Toast.LENGTH_SHORT).show()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }
}
