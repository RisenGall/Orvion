package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.text.Layout
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.room.Room
import com.mirackalyoncu.orvion.databinding.FragmentGirisBinding
import com.mirackalyoncu.orvion.model.Kullanici
import com.mirackalyoncu.orvion.roomdb.KullaniciDao
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class GirisFragment : Fragment() {

    private var _binding: FragmentGirisBinding? = null
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
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.grsDevamButton.setOnClickListener {

            val email = binding.grsMailEditText.text.toString()

            if (mailKontrol(email)) {

                mailGetir()



            } else {

                binding.grsMailEditText.error = "Geçerli bir e-posta adresi girin"
            }
        }

    }


    private fun mailGetir() {
        val email = binding.grsMailEditText.text.toString()

        mDisposable.add(
            kullaniciDao.kullaniciVarMi(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ kullanici ->

                    val action = GirisFragmentDirections.actionGirisFragmentToSifreGirisFragment(
                        bilgi = "eski",
                        id = kullanici.kullaniciId,
                        email = email
                    )
                    Navigation.findNavController(requireView()).navigate(action)
                }, {

                    val action = GirisFragmentDirections.actionGirisFragmentToKayitFragment(
                        bilgi = "yeni",
                        email = email
                    )
                    Navigation.findNavController(requireView()).navigate(action)
                })
        )
    }





    fun mailKontrol(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        mDisposable.clear()
    }
}