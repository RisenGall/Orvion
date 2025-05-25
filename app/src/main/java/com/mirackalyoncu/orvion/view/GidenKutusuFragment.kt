package com.mirackalyoncu.orvion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirackalyoncu.orvion.adapter.MailAdapter
import com.mirackalyoncu.orvion.databinding.FragmentGidenKutusuBinding
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class GidenKutusuFragment : Fragment() {

    private var _binding: FragmentGidenKutusuBinding? = null
    private val binding get() = _binding!!

    private val args: GidenKutusuFragmentArgs by navArgs()
    private val disposable = CompositeDisposable()

    private lateinit var mailAdapter: MailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGidenKutusuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val kullaniciId = args.kullaniciId

        setupRecyclerView()
        observeGidenMailler(kullaniciId)
    }

    private fun setupRecyclerView() {
        val kullaniciDao = OrvionDatabase.getInstance(requireContext()).kullaniciDao()

        mailAdapter = MailAdapter(
            mailList = emptyList(),
            gidenKutusuMu = true,
            onMailClick = { mail ->

            },
            kullaniciDao = kullaniciDao
        )

        binding.gidenMailRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mailAdapter
        }
    }

    private fun observeGidenMailler(kullaniciId: Int) {
        val mailDao = OrvionDatabase.getInstance(requireContext()).mailDao()

        disposable.add(
            mailDao.gidenKutusu(kullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mailler ->
                    mailAdapter.mailList = mailler
                    mailAdapter.notifyDataSetChanged()
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }
}
