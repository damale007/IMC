package com.conadasoft.imccalculadorapesoideal.ui

import SQLLite
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.FragmentInicioBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.abs

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!
    private var mInterstitialAd: InterstitialAd? = null
    private var publicaAnuncio: Boolean = false

    companion object {
        var diaNac: Int = 0
        var mesNac: Int = 0
        var anoNac: Int = 0
        var fecha: String = ""
        var sexo: Int = 0
        var peso: Float = 0f
        var altura: Int = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        leerPreferences()

        if (diaNac == 0 && mesNac == 0 && anoNac == 0 && sexo == 0 && peso == 0f && altura == 0)
            abrirDatosPersonales()
        else {
            publicidad()
            rellenaTarjetas()
        }

        binding.fab.setOnClickListener {
            nuevoPeso()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        leerPreferences()

        Log.d("DIANAC: ", "Altura: $altura")
        Log.d("DIANAC: ", "Peso: $peso")
        Log.d("DIANAC: ", "Sexo: $sexo")
        Log.d("DIANAC: ", "Año: $anoNac")
        Log.d("DIANAC: ", "Mes: $mesNac")
        Log.d("DIANAC: ", "Día: $diaNac")


        if (diaNac == 0 && mesNac == 0 && anoNac == 0 && sexo == 0 && peso == 0f && altura == 0)
            requireActivity().finish()
        else {
            publicidad()
            rellenaTarjetas()
        }
    }

    private fun publicidad() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(
            requireContext(), "ca-app-pub-8408332664043957/2669866761", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    if (publicaAnuncio) {
                        publicaAnuncio = false
                        mInterstitialAd!!.show(requireActivity())
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("MainActivity", loadAdError.toString())
                    mInterstitialAd = null
                }
            })

    }

    private fun nuevoPeso() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ingresa tu peso actual")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL// Orientación vertical
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Establece los márgenes del LinearLayout
        params.setMargins(90, 20, 90, 20) // Izquierda, arriba, derecha, abajo
        layout.layoutParams = params

        // Crea el EditText
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "En Kgs."
        layout.addView(input)
        builder.setView(layout)

        // Agrega los botones
        builder.setPositiveButton("Aceptar") { dialog, which ->
            peso = input.text.toString().toFloat()
            val c: Calendar = Calendar.getInstance()
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month = c.get(Calendar.MONTH) +1
            val year = c.get(Calendar.YEAR)

            fecha = "$year-$month-$day"

            val baseDatosSQL = SQLLite(requireActivity())
            val db = baseDatosSQL.writableDatabase
            baseDatosSQL.inicia(db)

            baseDatosSQL.insertaDiario(fecha, peso)
            baseDatosSQL.cierra(db)

            rellenaTarjetas()

            publicaAnuncio = true
            if (mInterstitialAd != null) {
                publicaAnuncio = false
                mInterstitialAd!!.show(requireActivity())
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun rellenaTarjetas() {
        var cadena = ""

        //Sexo
        if (sexo!=0)
            if (sexo == 1)
                cadena = "Masculino "
            else {
                cadena = "Femenino "
                binding.imagenGenero.setImageResource(R.drawable.icono_fem)
            }


        //Edad
        val edad = edadUsuario()
        if (edad<1000)
            cadena += "$edad años"

        binding.idSexoEdad.text = cadena

        //Altura
        if (altura!=0)
            binding.idAltura.text = "$altura cm de altura"
        else
            binding.idAltura.text = "Sin altura"

        binding.idPesoU.text = "$peso"

        //Peso ideal
        val ideal = pesoIdeal()
        binding.idIdeal.text = "peso ideal: $ideal kgs."

        var diferencia = peso - ideal
        if (abs(diferencia) <3f)
            binding.idPesoResultado.text="Tienes el peso ideal"
        else if (diferencia > 0)
            binding.idPesoResultado.text="Tienes sobrepeso de $diferencia kgs."
        else {
            diferencia = -diferencia
            binding.idPesoResultado.text = "Tienes desnutrición de $diferencia kgs."
        }

        val fechaFormateada = "(" + formatoFecha(fecha) + ")"
        binding.idFechaPeso.text = fechaFormateada

        //IMC
        val altMetros: Float = altura.toFloat() /100
        val imc: Float = peso / (altMetros * altMetros)

        val imcImprimir = String.format("%.2f", imc)
        binding.idIMC.text = imcImprimir

        val textoIMC = compararIMC(imc)
        binding.idIMCIdeal.text = textoIMC
    }

    private fun formatoFecha(fecha: String): String {
        val partes = fecha.split("-")
        val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

        return "${partes[2]} ${meses[partes[1].toInt() - 1]} ${partes[0]}"
    }

    private fun compararIMC(imc: Float): String {
        val texto = when (imc) {
            in 0f..18.5f -> "IMC inferior a lo normal"
            in 18.51f..24.9f -> "IMC normal"
            in 24.91f..29.9f -> "IMC superior al normal"
            in 29.91f..100f -> "Obesidad"
            else -> "IMC erróneo"
        }
        mueveAguja(imc)
        return texto
    }

    private fun mueveAguja(imc: Float) {

        val valor: Float = when (imc) {
            in 0f..8.99f -> -90f
            in 9f..18.5f -> -58 - (58 - (imc * 58 / 18.5f))
            in 18.51f..24.9f -> -58 + (58 / 6.39f) * (imc - 18.51f)
            in 24.91f..29.9f -> (58 / 4.99f) * (imc - 24.91f)
            in 29.91f..35f -> 58 + (32 / 5.09f) * (imc - 29.91f)
            else -> 90f
        }

        // Crear la animación de rotación
        val rotateAnimation = RotateAnimation(
            0f, valor, // Grados de rotación
            RotateAnimation.RELATIVE_TO_SELF, 0.5f, // Punto de pivote X (centro de la imagen)
            RotateAnimation.RELATIVE_TO_SELF, 1f  // Punto de pivote Y (centro de la imagen)
        ).apply {
            duration = 2000 // Duración en milisegundos (2 segundos)
            repeatCount = 0 // Repetir indefinidamente
            interpolator = LinearInterpolator() // Rotación suave
            fillAfter = true // Mantener la posición final
        }

        // Iniciar la animación
        binding.aguja.startAnimation(rotateAnimation)

    }

    private fun pesoIdeal() : Int{
        var ideal = 0

        if (sexo ==0)
            return 0
        else {
            if (sexo == 1) {  //Hombre
                ideal = when (altura) {
                    in 152..156 -> 51
                    in 157..161 -> 54
                    in 162..166 -> 57
                    in 167..171 -> 63
                    in 172..176 -> 66
                    in 177..181 -> 69
                    in 182..186 -> 74
                    in 187..191 -> 77
                    in 192..220 -> 80
                    else -> 0
                }
            } else {  // Mujer
                ideal = when (altura) {
                    in 152..156 -> 49
                    in 157..161 -> 52
                    in 162..166 -> 55
                    in 167..171 -> 59
                    in 172..176 -> 63
                    in 177..181 -> 67
                    in 182..186 -> 71
                    in 187..191 -> 75
                    in 192..220 -> 78
                    else -> 0
                }
            }
        }
        return ideal
    }

    private fun edadUsuario(): Int {
        val now = LocalDateTime.now()
        val year = now.year
        val mes = now.monthValue
        val dia = now.dayOfMonth

        var miEdad = year - anoNac

        if (mesNac > mes)
            miEdad--;
        else
            if (mesNac == mes && diaNac > dia) miEdad--;

        return miEdad
    }

    private fun abrirDatosPersonales() {
        val intent = Intent(requireContext(), DatosPersonales::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun leerPreferences(){
        val sharedPreferences = requireActivity().getSharedPreferences("Preferencias", Context.MODE_PRIVATE)

        diaNac = sharedPreferences.getInt("diaNac", 0)
        mesNac = sharedPreferences.getInt("mesNac", 0)
        anoNac = sharedPreferences.getInt("anoNac", 0)
        sexo = sharedPreferences.getInt("sexo", 0)
        altura = sharedPreferences.getInt("altura", 0)

        val baseDatosSQL = SQLLite(requireContext())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)
        val datos = baseDatosSQL.Select("")
        baseDatosSQL.cierra(db)

        if (datos.size > 0) {
            peso = datos[0][2].toString().toFloat()
            fecha = datos[0][1].toString()
        }
    }
}