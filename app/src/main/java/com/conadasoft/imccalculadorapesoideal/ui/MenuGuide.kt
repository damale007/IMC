package com.conadasoft.imccalculadorapesoideal.ui

import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.ActivityMenuGuideBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Locale
import kotlin.math.abs

class MenuGuide : AppCompatActivity() {
    private lateinit var binding: ActivityMenuGuideBinding
    private var mInterstitialAd: InterstitialAd? = null
    private var publicaAnuncio: Boolean = false
    private val actividad = this

    override fun onCreate(savedInstanceState: Bundle?) {
        val calDesayuno :IntArray = intArrayOf(350, 400, 550, 700)
        val calAlmuerzo :IntArray = intArrayOf(400, 550, 800, 900)
        val calCena :IntArray = intArrayOf(450, 500, 650, 750)
        val calMerienda :IntArray = intArrayOf(200, 350, 550, 580)

        super.onCreate(savedInstanceState)
        binding = ActivityMenuGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tmb: Float = intent.getFloatExtra("tmb", 0f)

        publicidad()


        var queda = tmb
        val principales :Float = tmb / 3 * 2

        val indAlmuerzo = eligeCalorias(calAlmuerzo, principales / 2)
        queda -= calAlmuerzo[indAlmuerzo]
        var quedaPrincipal = principales - calAlmuerzo[indAlmuerzo]

        val indCena = eligeCalorias(calCena, quedaPrincipal)
        queda -= calCena[indCena]

        val indDesayuno = eligeCalorias(calDesayuno, queda /2)
        queda -= calDesayuno[indDesayuno]

        val indMerienda = eligeCalorias(calMerienda, queda)
        val totalCalorias = calDesayuno[indDesayuno] + calAlmuerzo[indAlmuerzo] + calCena[indCena] + calMerienda[indMerienda]
        val formatoEspanol = NumberFormat.getNumberInstance(Locale("es", "ES"))
        val numero1 = formatoEspanol.format(totalCalorias)
        val numero2 = formatoEspanol.format(tmb)
        val textoCalorias = resources.getString(R.string.menu) + numero1 + resources.getString(R.string.mediaconsumo) + numero2 + resources.getString(R.string.caloriasDia)
        binding.totalCalorias.text = textoCalorias


        var menu: Array<String> = resources.getStringArray(R.array.desayunos)
        var titulo: String = calDesayuno[indDesayuno].toString() + " cal"
        binding.caloriasDesayuno.text = titulo
        binding.descDesayuno.text = menu[indDesayuno]

        menu = resources.getStringArray(R.array.almuerzos)
        titulo = calAlmuerzo[indAlmuerzo].toString() + " cal"
        binding.caloriasAlmuerzo.text = titulo
        binding.descAlmuerzo.text = menu[indAlmuerzo]

        menu = resources.getStringArray(R.array.cenas)
        titulo = calCena[indCena].toString() + " cal"
        binding.caloriasCena.text = titulo
        binding.descCena.text = menu[indCena]

        menu = resources.getStringArray(R.array.meriendas)
        titulo = calMerienda[indMerienda].toString() + " cal"
        binding.caloriasSnack.text = titulo
        binding.descSnack.text = menu[indMerienda]
    }

    //400, 550, 800, 900)
    private fun eligeCalorias(calorias: IntArray, tmb: Float): Int {
        var voy = 0
        for (i in calorias) {
            if (abs(i-tmb) < 250  && i-tmb > 0) {
                return voy
            }
            voy++;
        }
        return --voy
    }

    private fun publicidad() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(
            binding.adView.context, "ca-app-pub-8408332664043957/2669866761", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    if (publicaAnuncio) {
                        publicaAnuncio = false
                        mInterstitialAd!!.show(actividad)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("MainActivity", loadAdError.toString())
                    mInterstitialAd = null
                }
            })

    }
}