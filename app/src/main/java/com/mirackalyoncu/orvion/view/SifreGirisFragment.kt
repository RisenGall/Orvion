package com.mirackalyoncu.orvion.view

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.mirackalyoncu.orvion.databinding.FragmentSifreGirisBinding
import com.mirackalyoncu.orvion.roomdb.KullaniciDao
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SifreGirisFragment : Fragment() {

    private var _binding: FragmentSifreGirisBinding? = null
    private val binding get() = _binding!!

    private val mDisposable = CompositeDisposable()
    private lateinit var db: OrvionDatabase
    private lateinit var kullaniciDao: KullaniciDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = OrvionDatabase.getInstance(requireContext())
        kullaniciDao = db.kullaniciDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSifreGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val email = arguments?.let { SifreGirisFragmentArgs.fromBundle(it).email }

        binding.sfrDevamButton.setOnClickListener {
            val sifre = binding.sfrSifreEditText.text.toString()

            if (sifre.isNotEmpty() && !email.isNullOrEmpty()) {
                loginUser(email, sifre)
            } else {
                Toast.makeText(requireContext(), "Lütfen e-posta ve şifre girin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, sifre: String) {
        mDisposable.add(
            kullaniciDao.getKullaniciByEmailAndSifre(email, sifre)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ kullanici ->
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                    val action = SifreGirisFragmentDirections.actionSifreGirisFragmentToAnaSayfaFragment(id = kullanici.kullaniciId)
                    Navigation.findNavController(requireView()).navigate(action)
                }, { error ->
                    Toast.makeText(requireContext(), "Hatalı giriş, şifrenizi kontrol edin!!", Toast.LENGTH_SHORT).show()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.clear()
        _binding = null
    }
}
