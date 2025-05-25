package com.mirackalyoncu.orvion.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mirackalyoncu.orvion.R

import com.mirackalyoncu.orvion.databinding.FragmentSurecOlusturBinding
import com.mirackalyoncu.orvion.databinding.GorevlerCardviewBinding
import com.mirackalyoncu.orvion.model.Gorev
import com.mirackalyoncu.orvion.model.Kullanici
import com.mirackalyoncu.orvion.model.Surec
import com.mirackalyoncu.orvion.model.SurecKatilim
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.random.Random

class SurecOlusturFragment : Fragment() {

    private var _binding: FragmentSurecOlusturBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()
    private val gorevListesi = mutableListOf<GorevInput>()
    private lateinit var kullaniciListesi: List<Kullanici>
    private var kullaniciId: Int = -1
    private var girisKodu: Int = 0


    private val selectedUserIds = mutableListOf<Int>()




    data class GorevInput(val baslik: String, val aciklama: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurecOlusturBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            kullaniciId = it.getInt("kullaniciId")
        }

        kullanicilariYukle()
        benzersizGirisKoduUret { kod ->
            girisKodu = kod
            binding.tvGirisKodu.text = "Giriş Kodu: $girisKodu"
        }
        binding.tvSorumlularSecim.setOnClickListener {
            if (!::kullaniciListesi.isInitialized || kullaniciListesi.isEmpty()) {
                Toast.makeText(requireContext(), "Kullanıcılar yükleniyor veya yok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kullaniciIsimleri = kullaniciListesi.map { it.ad+" "+it.soyad }.toTypedArray()
            val seciliDizisi = BooleanArray(kullaniciListesi.size) { index ->
                selectedUserIds.contains(kullaniciListesi[index].kullaniciId)


            }

            AlertDialog.Builder(requireContext())
                .setTitle("Sorumlu Kişileri Seç")
                .setMultiChoiceItems(kullaniciIsimleri, seciliDizisi) { _, which, isChecked ->
                    if (isChecked) {

                        if (!selectedUserIds.contains(kullaniciListesi[which].kullaniciId)) {
                            selectedUserIds.add(kullaniciListesi[which].kullaniciId)
                        }
                    } else {

                        selectedUserIds.remove(kullaniciListesi[which].kullaniciId)
                    }
                }
                .setPositiveButton("Tamam") { dialog, _ ->

                    val secilenIsimler = kullaniciListesi.filter { selectedUserIds.contains(it.kullaniciId) }.map { it.ad+" "+it.soyad}
                    binding.tvSorumlularSecim.text = if (secilenIsimler.isEmpty()) {
                        "Sorumlu kişileri seçin"
                    } else {
                        secilenIsimler.joinToString(", ")
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("İptal", null)
                .show()
        }



        binding.tvGirisKodu.setOnClickListener {
            val textToCopy = binding.tvGirisKodu.text.toString()
                .removePrefix("Giriş Kodu: ")
                .trim()

            if (textToCopy.isNotBlank()) {
                val clipboard = ContextCompat.getSystemService(
                    requireContext(),
                    ClipboardManager::class.java
                )

                if (clipboard != null) {
                    val clip = ClipData.newPlainText("Giriş Kodu", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "Kod kopyalandı: $textToCopy", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Clipboard kullanılamıyor!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.etBitisTarihi.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),R.style.CustomDatePickerDialog,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val formattedDate = String.format("%02d.%02d.%04d", selectedDayOfMonth, selectedMonth + 1, selectedYear)
                    binding.etBitisTarihi.setText(formattedDate)
                }, year, month, day)

            datePickerDialog.setOnShowListener {

                val positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                val negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)


                positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))


                positiveButton.text = "Seç"
                negativeButton.text = "İptal"
            }
            datePickerDialog.show()

        }




        binding.btnGorevEkle.setOnClickListener {
            gorevEkle()
        }

        binding.btnKaydet.setOnClickListener {
            surecKaydet()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {


                    if (
                        binding.etSurecAdi.text.toString().isNotEmpty() ||
                        gorevListesi.isNotEmpty() ||
                        binding.etBitisTarihi.text.toString().isNotEmpty()
                    ) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Uyarı")
                            .setMessage("Formu kaydetmeden çıkmak üzeresiniz. Devam etmek istiyor musunuz?")
                            .setPositiveButton("Evet") { _, _ ->
                                findNavController().popBackStack()
                            }
                            .setNegativeButton("Hayır", null)
                            .show()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        )



    }

    private fun kullanicilariYukle() {
        val disposable = OrvionDatabase.getInstance(requireContext()).kullaniciDao()
            .getAllKullanicilar()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ liste ->
                kullaniciListesi = liste.filter { it.rol != "admin" }
                if (isAdded && _binding != null) {

                    val secilenIsimler = kullaniciListesi.filter { selectedUserIds.contains(it.kullaniciId) }.map { it.ad+" "+it.soyad }
                    binding.tvSorumlularSecim.text = if (secilenIsimler.isEmpty()) {
                        "Sorumlu kişileri seçin"
                    } else {
                        secilenIsimler.joinToString(", ")
                    }

                    if (kullaniciListesi.isEmpty()) {
                        Toast.makeText(requireContext(), "Hiç kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
            }, { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Kullanıcılar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            })

        compositeDisposable.add(disposable)
    }

    private fun gorevEkle() {
        val baslik = binding.etGorevBaslik.text.toString().trim()
        val aciklama = binding.etGorevAciklama.text.toString().trim()

        if (baslik.isEmpty()) {
            binding.etGorevBaslik.error = "Görev başlığı boş olamaz"
            return
        }

        if (aciklama.isEmpty()) {
            binding.etGorevAciklama.error = "Görev açıklaması boş olamaz"
            return
        }

        gorevListesi.add(GorevInput(baslik, aciklama))
        binding.etGorevBaslik.text?.clear()
        binding.etGorevAciklama.text?.clear()

        gorevGoster()
    }

    private fun gorevGoster() {
        binding.gorevlerContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        gorevListesi.forEachIndexed { index, gorev ->


            val itemBinding = GorevlerCardviewBinding.inflate(inflater, binding.gorevlerContainer, false)

            itemBinding.tvGorevAdi.text = "${index + 1}. ${gorev.baslik}"


            itemBinding.btnSil.setOnClickListener {
                gorevListesi.removeAt(index)
                gorevGoster()
            }


            binding.gorevlerContainer.addView(itemBinding.root)
        }
    }



    private fun surecKaydet() {
        val surecAdi = binding.etSurecAdi.text.toString().trim()
        val bitisTarihi = binding.etBitisTarihi.text.toString().trim()

        if (surecAdi.isEmpty()) {
            binding.etSurecAdi.error = "Süreç adı boş olamaz"
            return
        }

        if (bitisTarihi.isEmpty()) {
            binding.etBitisTarihi.error = "Bitiş tarihi boş olamaz"
            return
        }

        if (selectedUserIds.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen en az bir sorumlu seçin", Toast.LENGTH_SHORT).show()
            return
        }

        if (gorevListesi.isEmpty()) {
            Toast.makeText(requireContext(), "En az bir görev ekleyin", Toast.LENGTH_SHORT).show()
            return
        }

        val olusturmaTarihi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        val surec = Surec(
            surecAdi = surecAdi,
            bitisTarihi = bitisTarihi,
            olusturanKullaniciId = if (kullaniciId != -1) kullaniciId else null,
            olusturmaTarihi = olusturmaTarihi,
            girisKodu = girisKodu
        )

        val db = OrvionDatabase.getInstance(requireContext())

        compositeDisposable.add(
            db.surecDao().insertSurec(surec)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ surecIdLong ->

                    val surecId = surecIdLong.toInt()


                    val katilimList = selectedUserIds.map { kullaniciId ->
                        SurecKatilim(surecId = surecId, kullaniciId = kullaniciId)
                    }

                    compositeDisposable.add(
                        db.surecDao().insertSurecKatilim(katilimList)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                val gorevler = gorevListesi.map { input ->
                                    Gorev(
                                        surecId = surecId,
                                        baslik = input.baslik,
                                        aciklama = input.aciklama,
                                        sonTarih = bitisTarihi,
                                        durum = "Beklemede"
                                    )
                                }

                                compositeDisposable.add(
                                    db.gorevDao().insertGorevList(gorevler)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            Toast.makeText(requireContext(), "Süreç başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                                            temizle()
                                            findNavController().navigateUp()
                                        }, { error ->
                                            error.printStackTrace()
                                            Toast.makeText(requireContext(), "Görevler kaydedilirken hata oluştu", Toast.LENGTH_SHORT).show()
                                        })
                                )

                            }, { error ->
                                error.printStackTrace()
                                Toast.makeText(requireContext(), "Süreç katılımcıları kaydedilirken hata oluştu", Toast.LENGTH_SHORT).show()
                            })
                    )

                }, { error ->
                    error.printStackTrace()
                    Toast.makeText(requireContext(), "Süreç kaydedilirken hata oluştu", Toast.LENGTH_SHORT).show()
                })
        )
    }

    private fun benzersizGirisKoduUret(onComplete: (Int) -> Unit) {
        val dao = OrvionDatabase.getInstance(requireContext()).surecDao()

        fun kodUretVeKontrolEt(kalanDeneme: Int = 10) {
            if (kalanDeneme <= 0) {
                Toast.makeText(requireContext(), "Giriş kodu oluşturulamadı", Toast.LENGTH_SHORT).show()
                return
            }

            val yeniKod = Random.nextInt(1000, 9999)
            compositeDisposable.add(
                dao.getSurecByGirisKodu(yeniKod)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->

                        kodUretVeKontrolEt(kalanDeneme - 1)
                    }, {
                        onComplete(yeniKod)
                    })
            )
        }

        kodUretVeKontrolEt()
    }


    private fun temizle() {
        binding.etSurecAdi.text?.clear()
        binding.etBitisTarihi.text?.clear()
        gorevListesi.clear()
        binding.gorevlerContainer.removeAllViews()

        benzersizGirisKoduUret { kod ->
            girisKodu = kod
            binding.tvGirisKodu.text = "Giriş Kodu: $girisKodu"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        _binding = null
    }
}
