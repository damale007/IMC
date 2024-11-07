package com.conadasoft.imccalculadorapesoideal.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conadasoft.imccalculadorapesoideal.databinding.FragmentAppsBinding

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)

        eventos()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun eventos() {
        binding.logoAmor.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.callecaboapp.callecabo.amordiario&hl=es_419"))
            startActivity(intent)
        }

        binding.tituloAmor.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.callecaboapp.callecabo.amordiario&hl=es_419"))
            startActivity(intent)
        }

        binding.logoChiste.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.chistediario&hl=es"))
            startActivity(intent)
        }

        binding.tituloChiste.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.chistediario&hl=es"))
            startActivity(intent)
        }

        binding.logoDiario.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.simplediario"))
            startActivity(intent)
        }

        binding.tituloDiario.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.simplediario"))
            startActivity(intent)
        }

        binding.logoRosario.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.rosariodiario&hl=es"))
            startActivity(intent)
        }

        binding.tituloRosario.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.rosariodiario&hl=es"))
            startActivity(intent)
        }

        binding.logoColors.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conada.colors"))
            startActivity(intent)
        }

        binding.tituloColors.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conada.colors"))
            startActivity(intent)
        }

        binding.logoPepeJones.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.PepeJones&hl=es_419"))
            startActivity(intent)
        }

        binding.tituloPepeJones.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.conadasoft.PepeJones&hl=es_419"))
            startActivity(intent)
        }
    }

}