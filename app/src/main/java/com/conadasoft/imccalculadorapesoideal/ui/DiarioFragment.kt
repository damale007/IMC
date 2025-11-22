package com.conadasoft.imccalculadorapesoideal.ui

import SQLLite
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.conadasoft.imccalculadorapesoideal.BuildConfig
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.FragmentDiarioBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class DiarioFragment : Fragment() {

    private var _binding: FragmentDiarioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiarioBinding.inflate(inflater, container, false)

        binding.adView.adUnitId = BuildConfig.UNIT_ID

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val baseDatosSQL = SQLLite(requireActivity())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)

        val pesos = baseDatosSQL.Select("")

        val items = mutableListOf<MutableList<String>>()

        var contador = 1;
        for (peso in pesos) {
            items.add(mutableListOf(contador.toString(), peso.fecha, peso.peso.toString()))
            contador++
        }

        // Asignar el adaptador al ListView
        binding.recyclerView.adapter = RecyclerAdapter(items)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}