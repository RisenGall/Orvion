package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.mirackalyoncu.orvion.databinding.FragmentKayitBinding
import com.mirackalyoncu.orvion.model.Kullanici
import com.mirackalyoncu.orvion.roomdb.KullaniciDao
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class KayitFragment : Fragment() {

    private var _binding: FragmentKayitBinding? = null

    private val binding get() = _binding!!

    private val mDisposable = CompositeDisposable()

    private lateinit var db : OrvionDatabase
    private lateinit var kullaniciDao : KullaniciDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = OrvionDatabase.getInstance(requireContext())

        kullaniciDao = db.kullaniciDao()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val email = arguments?.let { KayitFragmentArgs.fromBundle(it).email }


        email?.let {
            binding.mailEditText.setText(it)
        }

        binding.kaydetButton.setOnClickListener {
            val ad = binding.isimEditText.text.toString()
            val soyad = binding.soyIsimEditText.text.toString()
            val email = binding.mailEditText.text.toString()
            val sifre = binding.sfrSifreEditText.text.toString()
            val sifreTekrar = binding.sifreDogrulamaEditText.text.toString()
            val adminKodu = binding.adminNumberEditText.text.toString()

            if (sifre != sifreTekrar) {
                Toast.makeText(requireContext(), "Şifreler uyuşmuyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = if (adminKodu == "123") "admin" else "kullanici"

            val yeniKullanici = Kullanici(
                ad = ad,
                soyad = soyad,
                email = email,
                sifre = sifre,
                rol = rol
            )

            mDisposable.add(
                kullaniciDao.insertKullanici(yeniKullanici)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        mDisposable.add(
                            kullaniciDao.getKullaniciByEmail(email)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ eklenenKullanici ->
                                    Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show()

                                    val action = KayitFragmentDirections
                                        .actionKayitFragmentToAnaSayfaFragment(
                                            id = eklenenKullanici.kullaniciId
                                        )

                                    Navigation.findNavController(view).navigate(action)

                                }, { hata ->
                                    Toast.makeText(requireContext(), "ID alma hatası: ${hata.message}", Toast.LENGTH_SHORT).show()
                                })
                        )
                    }, { error ->
                        Toast.makeText(requireContext(), "Kayıt hatası: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.clear()
        _binding = null
    }
}