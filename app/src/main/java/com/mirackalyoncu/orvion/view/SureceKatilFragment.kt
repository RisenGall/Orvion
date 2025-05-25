package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.mirackalyoncu.orvion.databinding.FragmentSureceKatilBinding
import com.mirackalyoncu.orvion.model.SurecKatilim
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SureceKatilFragment : Fragment() {
    private var _binding: FragmentSureceKatilBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: OrvionDatabase
    private val compositeDisposable = CompositeDisposable()
    private val args: SureceKatilFragmentArgs by navArgs()
    private val aktifKullaniciId by lazy { args.kullaniciId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = OrvionDatabase.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSureceKatilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.girisButonu.setOnClickListener {
            val girilenGirisKodu = binding.girisKoduEditText.text.toString().toIntOrNull()

            if (girilenGirisKodu == null) {
                Toast.makeText(requireContext(), "Geçerli bir giriş kodu girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            compositeDisposable.add(
                db.surecDao().getSurecByGirisKodu(girilenGirisKodu)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ surec ->

                        compositeDisposable.add(
                            db.surecKatilimDao().getKullaniciSurecleri(aktifKullaniciId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ surecler ->
                                    val zatenKatilmis = surecler.any { it.surecId == surec.surecId }

                                    if (zatenKatilmis) {
                                        Toast.makeText(requireContext(), "Zaten bu sürece katıldınız", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val yeniKatilim = SurecKatilim(surecId = surec.surecId, kullaniciId = aktifKullaniciId)
                                        compositeDisposable.add(
                                            db.surecKatilimDao().kullaniciyiSureceEkle(yeniKatilim)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({
                                                    Toast.makeText(requireContext(), "Sürece başarıyla katıldınız", Toast.LENGTH_SHORT).show()

                                                }, { e ->
                                                    Toast.makeText(requireContext(), "Katılma hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                })
                                        )
                                    }
                                }, { e ->
                                    Toast.makeText(requireContext(), "Katılım kontrol hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                })
                        )
                    }, {
                        Toast.makeText(requireContext(), "Geçersiz giriş kodu", Toast.LENGTH_SHORT).show()
                    })
            )

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }
}
