package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirackalyoncu.orvion.adapter.SurecAdapter
import com.mirackalyoncu.orvion.databinding.FragmentAnaSayfaBinding
import com.mirackalyoncu.orvion.roomdb.KullaniciDao
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import com.mirackalyoncu.orvion.roomdb.SurecDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AnaSayfaFragment : Fragment() {

    private var _binding: FragmentAnaSayfaBinding? = null
    private val binding get() = _binding!!

    private val mDisposable = CompositeDisposable()

    private lateinit var db: OrvionDatabase
    private lateinit var surecDao: SurecDao
    private lateinit var kullaniciDao: KullaniciDao

    private lateinit var surecAdapter: SurecAdapter

    private val args: AnaSayfaFragmentArgs by navArgs()

    private var kullaniciId: Int = 0
    private var kullaniciRol: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnaSayfaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = OrvionDatabase.getInstance(requireContext())
        surecDao = db.surecDao()
        kullaniciDao = db.kullaniciDao()

        kullaniciId = args.id


        binding.sureclerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        surecAdapter = SurecAdapter(emptyList()) { surec ->

        }
        binding.sureclerRecyclerView.adapter = surecAdapter


        mDisposable.add(
            kullaniciDao.getKullaniciById(kullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { kullanici ->
                    kullaniciRol = kullanici.rol
                    val rol = kullanici.rol

                    val source = if (rol == "admin") {
                        surecDao.getSurecWithProgressByAdminId(kullaniciId)
                    } else {
                        surecDao.getSurecWithProgressByUserId(kullaniciId)
                    }

                    source.firstOrError().map { surecList ->
                        Pair(rol, surecList.isEmpty())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (rol, isEmpty) ->
                    if (rol == "admin") {
                        binding.surecOlusturImageView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        binding.sureceDahilOlImageView.visibility = View.GONE
                    } else {
                        binding.sureceDahilOlImageView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        binding.surecOlusturImageView.visibility = View.GONE
                    }
                }, { error ->
                    error.printStackTrace()
                })
        )


        binding.artiImageButton.setOnClickListener {
            when (kullaniciRol) {
                "admin" -> {
                    val action = AnaSayfaFragmentDirections.actionAnaSayfaFragmentToSurecOlusturFragment(kullaniciId)
                    Navigation.findNavController(requireView()).navigate(action)
                }

                else -> {
                    val action = AnaSayfaFragmentDirections.actionAnaSayfaFragmentToSureceKatilFragment(kullaniciId)
                    Navigation.findNavController(requireView()).navigate(action)
                }
            }
        }


        binding.gorevImageButton.setOnClickListener {
            val rol = kullaniciRol
            if (rol != null) {
                val action = AnaSayfaFragmentDirections.actionAnaSayfaFragmentToGorevListesiFragment(
                    kullaniciId = kullaniciId,

                )
                Navigation.findNavController(requireView()).navigate(action)
            } else {
                Toast.makeText(requireContext(), "Kullanıcı rolü alınamadı", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mailImageButton.setOnClickListener {
            val action = AnaSayfaFragmentDirections.actionAnaSayfaFragmentToGelenKutusuFragment(kullaniciId=kullaniciId)
            Navigation.findNavController(requireView()).navigate(action)
        }

        binding.logOutImageButton.setOnClickListener {
            val action = AnaSayfaFragmentDirections.actionAnaSayfaFragmentToGirisFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()


        mDisposable.add(
            kullaniciDao.getKullaniciById(kullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { kullanici ->
                    kullaniciRol = kullanici.rol
                    val source = if (kullanici.rol == "admin") {
                        surecDao.getSurecWithProgressByAdminId(kullaniciId)
                    } else {
                        surecDao.getSurecWithProgressByUserId(kullaniciId)
                    }
                    source.firstOrError()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ surecList ->
                    surecAdapter.updateData(surecList)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}
