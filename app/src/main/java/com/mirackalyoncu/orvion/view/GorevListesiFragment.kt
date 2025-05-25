package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirackalyoncu.orvion.adapter.GorevAdapter
import com.mirackalyoncu.orvion.databinding.FragmentGorevListesiBinding
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class GorevListesiFragment : Fragment() {

    private var _binding: FragmentGorevListesiBinding? = null
    private val binding get() = _binding!!

    private val mDisposable = CompositeDisposable()

    private lateinit var gorevAdapter: GorevAdapter


    private val args: GorevListesiFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGorevListesiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = OrvionDatabase.getInstance(requireContext())
        val gorevDao = db.gorevDao()
        val surecDao = db.surecDao()
        val kullaniciId = args.kullaniciId

        gorevAdapter = GorevAdapter(requireContext(), mutableListOf(), emptyMap()) { guncellenmisGorev ->
            mDisposable.add(
                gorevDao.updateGorev(guncellenmisGorev)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({}, { error ->
                        Toast.makeText(requireContext(), "Hata: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    })
            )
        }

        binding.gorevRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gorevAdapter
        }


        mDisposable.add(
            surecDao.getAllSurecler()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ surecList ->
                    val surecMap = surecList.associate { it.surecId to it.surecAdi }
                    gorevAdapter.updateSurecAdlariMap(surecMap)
                }, { error ->
                    Toast.makeText(requireContext(), "Süreçler yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
                })
        )


        mDisposable.add(
            gorevDao.getGorevlerBySureceDahilKullanici(kullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ gorevListesi ->
                    gorevAdapter.updateList(gorevListesi)
                }, { error ->
                    Toast.makeText(requireContext(), "Görev yüklenemedi: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                })
        )


        binding.anaSayfaImageButton.setOnClickListener {
            findNavController().navigate(
                GorevListesiFragmentDirections.actionGorevListesiFragmentToAnaSayfaFragment(kullaniciId)
            )
        }

        binding.mailImageButton.setOnClickListener {
            val action = GorevListesiFragmentDirections.actionGorevListesiFragmentToGelenKutusuFragment(kullaniciId)
            Navigation.findNavController(requireView()).navigate(action)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mDisposable.clear()
        _binding = null
    }
}
