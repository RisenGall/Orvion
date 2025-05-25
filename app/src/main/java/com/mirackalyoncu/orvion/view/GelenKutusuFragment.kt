package com.mirackalyoncu.orvion.view
import com.mirackalyoncu.orvion.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirackalyoncu.orvion.adapter.MailAdapter
import com.mirackalyoncu.orvion.databinding.FragmentGelenKutusuBinding
import com.mirackalyoncu.orvion.roomdb.OrvionDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers



class GelenKutusuFragment : Fragment() {

    private var _binding: FragmentGelenKutusuBinding? = null
    private val binding get() = _binding!!
    private val args: GelenKutusuFragmentArgs by navArgs()

    private val disposable = CompositeDisposable()
    private lateinit var mailAdapter: MailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGelenKutusuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val kullaniciId = args.kullaniciId

        setupRecyclerView(kullaniciId)
        gelenMailleriYukle(kullaniciId)
        setupFabMenu(kullaniciId)

        binding.anaSayfaImageButton.setOnClickListener {
            val action = GelenKutusuFragmentDirections.actionGelenKutusuFragmentToAnaSayfaFragment(kullaniciId)
            Navigation.findNavController(requireView()).navigate(action)
        }
        binding.gorevImageButton.setOnClickListener {
            val action = GelenKutusuFragmentDirections.actionGelenKutusuFragmentToGorevListesiFragment(kullaniciId)
            Navigation.findNavController(requireView()).navigate(action)
        }




    }

    private fun setupRecyclerView(kullaniciId: Int) {
        val database = OrvionDatabase.getInstance(requireContext())
        val mailDao = database.mailDao()
        val kullaniciDao = database.kullaniciDao()

        mailAdapter = MailAdapter(
            mailList = emptyList(),
            gidenKutusuMu = false,
            onMailClick = { tiklananMail ->
                if (!tiklananMail.okundu) {
                    tiklananMail.okundu = true
                    OrvionDatabase.getInstance(requireContext()).mailDao()
                        .updateMail(tiklananMail)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                        }, { it.printStackTrace() })
                        .let { disposable.add(it) }
                }
            },
            kullaniciDao = kullaniciDao
        )



        binding.gelenMailRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mailAdapter
        }
    }

    private fun gelenMailleriYukle(kullaniciId: Int) {
        val mailDao = OrvionDatabase.getInstance(requireContext()).mailDao()

        disposable.add(
            mailDao.gelenKutusu(kullaniciId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mailler ->
                    mailAdapter.mailList = mailler
                    mailAdapter.notifyDataSetChanged()
                }, { hata ->
                    hata.printStackTrace()
                })
        )
    }

    private fun setupFabMenu(kullaniciId: Int) {
        var menuAcik = false


        listOf(binding.fabMailGonderGroup, binding.fabGidenGroup).forEach {
            it.visibility = View.GONE
            it.scaleX = 0f
            it.scaleY = 0f
            it.alpha = 0f
        }

        binding.fab.setOnClickListener {
            menuAcik = !menuAcik
            val hedefScale = if (menuAcik) 1f else 0f
            val hedefAlpha = if (menuAcik) 1f else 0f

            listOf(binding.fabMailGonderGroup, binding.fabGidenGroup).forEach { view ->
                if (menuAcik) view.visibility = View.VISIBLE
                view.animate()
                    .scaleX(hedefScale)
                    .scaleY(hedefScale)
                    .alpha(hedefAlpha)
                    .setDuration(200)
                    .withEndAction {
                        if (!menuAcik) view.visibility = View.GONE
                    }
                    .start()
            }
        }

        binding.fabMailGonder.setOnClickListener {
            val action = GelenKutusuFragmentDirections
                .actionGelenKutusuFragmentToMailGonderFragment(kullaniciId)
            findNavController().navigate(action)
        }

        binding.fabGidenKutusu.setOnClickListener {
            val action = GelenKutusuFragmentDirections
                .actionGelenKutusuFragmentToGidenKutusuFragment(kullaniciId)
            findNavController().navigate(action)
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }
}
