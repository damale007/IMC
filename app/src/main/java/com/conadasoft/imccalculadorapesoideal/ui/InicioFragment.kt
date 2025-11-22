package com.conadasoft.imccalculadorapesoideal.ui

import SQLLite
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.conadasoft.imccalculadorapesoideal.BuildConfig
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.NumberFormat
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.FragmentInicioBinding
//import com.github.mikephil.charting.BuildConfig
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale
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
        var altura: Float = 0f
        var medidaPeso: Int = 0
        var medidaAlto: Int = 0
        var nombre: String = ""
        var actividad: Int = -1
        var versionApp: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        leerPreferences()


        val versionActual = getAppVersionName(requireContext());
        if (versionActual != versionApp) {
            actualizadb()
            grabaPreferencias()
        }

        if (diaNac == 0 && mesNac == 0 && anoNac == 0 && sexo == 0 && peso == 0f && altura == 0f)
            abrirDatosPersonales(true)
        else {
            publicidad()
            rellenaTarjetas()
        }

        binding.fab.setOnClickListener {
            nuevoPeso()
        }

        binding.settings.setOnClickListener {
            abrirDatosPersonales(false)
        }

        val currentLanguage = Locale.getDefault().language
        if (currentLanguage != "es")
            binding.barraMedidora.setImageResource(R.drawable.barra_medidora_ing)

        barra()

        binding.questionTMB.setOnClickListener {
            AlertDialog.Builder(binding.questionTMB.context)
                .setTitle(getString(R.string.TMB_titulo))
                .setMessage(getString(R.string.TMB_texto))
                .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.questionGEDT.setOnClickListener {
            AlertDialog.Builder(binding.questionTMB.context)
                .setTitle(getString(R.string.GEDT_titulo))
                .setMessage(getString(R.string.GEDT_texto))
                .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
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

        if (diaNac == 0 && mesNac == 0 && anoNac == 0 && sexo == 0 && peso == 0f && altura == 0f)
            requireActivity().finish()
        else {
            publicidad()
            rellenaTarjetas()
        }
    }

    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("GetVersion", "Error al obtener la versión de la app", e)
            "" // Retorna una cadena vacía en caso de error.
        }
    }

    private fun actualizadb() {
        val baseDatosSQL = SQLLite(requireActivity())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)

        val pesos = baseDatosSQL.Select("", false)
        pesos.forEachIndexed { index, peso ->
            if (peso.fecha.length < 10)
                baseDatosSQL.arreglaFecha(peso.id, peso.fecha)
        }
        baseDatosSQL.close()
    }

    private fun TMB(): Float {
        var tmb: Float = 0f

        if (sexo == 1)
            tmb = (10f * peso) + (6.25f * altura) - (5f * edadUsuario()) + 5
        else if (sexo == 2)
            tmb = (10f * peso) + (6.25f * altura) - (5f *edadUsuario()) - 161

        val formatoEspanol = NumberFormat.getNumberInstance(Locale("es", "ES"))
        val numeroFormateado = formatoEspanol.format(tmb)
        binding.idTMB.text = numeroFormateado
        return tmb
    }

    private fun GEDT(tmb: Float): Float {
        var gedt: Float = 0f
        val valores: Array<Float> = arrayOf(1.2f, 1.375f, 1.55f, 1.725f, 1.9f)

        if (actividad == -1) {
            binding.idGEDT.text = getString(R.string.GEDT_error)
            binding.cal.visibility = View.GONE
        } else {
            gedt = tmb * valores[actividad]

            val formatoEspanol = NumberFormat.getNumberInstance(Locale("es", "ES"))
            val numeroFormateado = formatoEspanol.format(gedt)

            binding.idGEDT.text = numeroFormateado
            binding.cal.visibility = View.VISIBLE
        }

        return gedt
    }

    private fun barra() {
        val barChart: LineChart = binding.chart
        val entries = mutableListOf<Entry>()
            val baseDatosSQL = SQLLite(requireActivity())
            val db = baseDatosSQL.writableDatabase
            baseDatosSQL.inicia(db)

            val fechaActual: LocalDate = LocalDate.now()
            val fechaInicio = fechaActual.minusMonths(1)

            val whereClause = "fecha >= '$fechaInicio'"
            val pesos = baseDatosSQL.Select(whereClause, false)
            baseDatosSQL.close()


        val xAxis: XAxis = barChart.xAxis
        xAxis.textColor = "#777777".toColorInt() // Cambia Color.RED al color que desees
        xAxis.gridColor = "#777777".toColorInt() // Un gris, por ejemplo. Usa el color que prefieras.
        xAxis.axisLineColor = Color.BLUE // Cambia Color.BLUE al color que desees

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.textColor = "#777777".toColorInt() // Cambia Color.GREEN al color que desees
        leftAxis.gridColor = "#777777".toColorInt() // Otro tono de gris. Usa el color que prefieras.
        leftAxis.axisLineColor = Color.MAGENTA // Cambia Color.MAGENTA al color que desees
        barChart.axisRight.isEnabled = false
        leftAxis.axisLineWidth= 2f

        val rightAxis: YAxis = barChart.axisRight
        rightAxis.textColor = "#777777".toColorInt()
        rightAxis.gridColor = "#777777".toColorInt()
        rightAxis.axisLineColor = Color.CYAN

        var ejeX = mutableListOf<String>()
        var contador = 0;
        pesos.forEachIndexed { index, peso ->
            entries.add(Entry(index.toFloat(), peso.peso))
            ejeX.add(formatoFecha(peso.fecha, true))
        }

        val dataSet = LineDataSet(entries, "Pesos")
        val lineData = LineData(dataSet)
        dataSet.lineWidth = 4f
        dataSet.color = "#7CA0FF".toColorInt() // Rosa pastel

        dataSet.setDrawFilled(true)

        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in ejeX.indices) ejeX[index] else ""
            }
        }

        xAxis.valueFormatter = formatter
        xAxis.granularity = 1f

        //Configura el gráfico
        barChart.data = lineData
        barChart.invalidate()
    }

    private fun publicidad() {
        binding.adView.adUnitId = BuildConfig.UNIT_ID

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        InterstitialAd.load(
            requireContext(), BuildConfig.ID_ADMOB, adRequest,
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
        val mensaje = resources.getString(R.string.pesoActual) + " " + resources.getStringArray(R.array.m_peso)[medidaPeso]
        builder.setTitle(mensaje)

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
        input.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = resources.getStringArray(R.array.m_peso)[medidaPeso]
        layout.addView(input)
        builder.setView(layout)

        // Agrega los botones
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
            try {
                peso = input.text.toString().toFloat()
                val c: Calendar = Calendar.getInstance()
                val day = c.get(Calendar.DAY_OF_MONTH)
                val month = c.get(Calendar.MONTH) + 1
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
            } catch (e: Exception) {
                val builder2 = AlertDialog.Builder(requireContext())
                val mensaje2 = resources.getString(R.string.errorPeso)
                builder2.setTitle(mensaje2)
                builder2.setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                    dialog.cancel()
                }
                builder2.show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun rellenaTarjetas() {
        tarjetaDatos()
        tarjetaPeso()
        tarjetaIMC()
        var tmb = TMB()
        val gedt = GEDT(tmb)

        binding.botonMenu.setOnClickListener {
            val intent = Intent(requireContext(), MenuGuide::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("tmb", gedt)
            startActivity(intent)
        }
    }

    private fun tarjetaIMC() {
        var alto = altura
        var pesoC = peso

        if (medidaAlto == 1)
            alto = (altura * 30.48).toFloat()

        if (medidaPeso == 1)
            pesoC *= 0.453592f

        val altMetros: Float = alto.toFloat() /100
        val imc: Float = pesoC / (altMetros * altMetros)

        val imcImprimir = String.format("%.2f", imc)
        binding.idIMC.text = imcImprimir

        val textoIMC = compararIMC(imc)
        binding.idIMCIdeal.text = textoIMC
    }

    private fun tarjetaPeso() {
        var mensaje: String
        binding.idPesoU.text = "$peso"
        binding.idUnidadPeso.text = resources.getStringArray(R.array.m_peso)[medidaPeso]

        //Peso ideal
        val ideal = pesoIdeal()
        mensaje = resources.getString(R.string.pesoIdeal) + " " + ideal + " " + resources.getStringArray(R.array.m_peso)[medidaPeso]
        binding.idIdeal.text = mensaje

        var diferencia: Int = (peso - ideal).toInt()


        val contenedor = binding.idBarraContenedora
        val barra = binding.idBarraVerde

        contenedor.post {
            val anchoContenedor = contenedor.width
            val porc: Float = diferencia.toFloat() / 50
            val anc: Float = anchoContenedor.toFloat() * (1-porc)
            val anchoNuevo: Int = anc.toInt()

            barra.layoutParams.width = anchoNuevo
            barra.requestLayout()
        }

        if (abs(diferencia) <3f)
            mensaje = resources.getString(R.string.pesoIdealT)
        else if (diferencia > 0)
            mensaje = resources.getString(R.string.sobrepeso) + " " + diferencia + " " + resources.getStringArray(R.array.m_peso)[medidaPeso]
        else {
            diferencia = -diferencia
            mensaje = resources.getString(R.string.bajopeso) + " " + diferencia + " " + resources.getStringArray(R.array.m_peso)[medidaPeso]
        }
        binding.idPesoResultado.text = mensaje

        /*if (abs(diferencia) > 0) {
            val mediaMes = calculaMediaMes()
            val tiempo = abs(diferencia) / mediaMes

           // if (tiempo>0 && tiempo<50) {
                var mensaje2 = getString(R.string.tiempoPesoIdeal1) + " " + tiempo + getString(R.string.tiempoPesoIdeal2)
                binding.diferenciaIdeal.text = mensaje2
                binding.diferenciaIdeal.visibility = View.VISIBLE
           // } else {
           //     binding.diferenciaIdeal.visibility = View.GONE
           // }
        }*/
    }

    private fun calculaMediaMes(): Float {
        val fechaActual: LocalDate = LocalDate.now()
        val fechaInicio = fechaActual.minusMonths(1)
        val baseDatosSQL = SQLLite(requireActivity())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)

        val pesos = baseDatosSQL.Select("fecha>='$fechaInicio'", false)
        baseDatosSQL.close()

        var ultPeso = pesos[0].peso

        return ultPeso - peso
    }

    private fun tarjetaDatos() {
        var cadena = ""

        if (nombre.isNotEmpty())
            binding.idNombre.text = nombre

        if (sexo == 1)
            cadena = resources.getString(R.string.masculino_dp)
        else if (sexo == 2)
            cadena = resources.getString(R.string.femenino_dp)
        //Edad
        val edad = edadUsuario()
        if (edad<1000)
            cadena += " " + edad + " " + resources.getString(R.string.years)

        binding.idSexoEdad.text = cadena
        var mensaje: String

        //Altura
        if (altura != 0f) {
            if (medidaAlto == 0)
                mensaje = altura.toString() + " " + resources.getStringArray(R.array.m_alto)[0] + " " + resources.getString(R.string.alto)
            else {
                val pies: Int = altura.toInt()
                val pulgadasRestantes: Int = (altura % 12).toInt()
                mensaje = pies.toString() + "\'" + pulgadasRestantes.toString() + "\'\'"
            }
            binding.idAltura.text = mensaje
        } else
            binding.idAltura.text = resources.getString(R.string.noAlto)

        //Actividad
        if (actividad != -1){
            binding.idActividad.text = resources.getStringArray(R.array.m_actividad)[actividad]
            binding.idActividad.visibility = View.VISIBLE
        } else {
            binding.idActividad.visibility = View.GONE
        }

    }

    private fun formatoFecha(fecha: String, corto: Boolean = false): String {
        val partes = fecha.split("-")

        if (!corto)
            return partes[2] + " " + resources.getStringArray(R.array.meses)[partes[1].toInt() -1] + " " + partes[0]
        else
            return partes[2] + " " + resources.getStringArray(R.array.meses)[partes[1].toInt() -1].subSequence(0, 3)
    }

    private fun compararIMC(imc: Float): String {
        val texto = when (imc) {
            in 0f..18.5f -> resources.getString(R.string.imcInf)
            in 18.51f..24.9f -> resources.getString(R.string.imcNormal)
            in 24.91f..29.9f -> resources.getString(R.string.imcAlto)
            in 29.91f..100f -> resources.getString(R.string.imcObesidad)
            else -> resources.getString(R.string.imcError)
        }
        mueveAguja(imc)
        return texto
    }

    private fun mueveAguja(imc: Float) {

        val valor: Float = when (imc) {
            in 0f..8.99f -> 10f
            in 9f..18.5f -> (imc - 9) * 15 / 9 + 10
            in 18.51f..24.9f -> (imc -18.51f) * 25 / 6 + 25
            in 24.91f..29.9f -> (imc - 24.91f) * 25 / 5 + 50
            in 29.91f..35f -> (imc - 29.91f) * 25 / 6 + 75
            else -> 90f
        }

        val layout = binding.barraMedidora

        layout.post {
            val ancho = layout.width
            val x = ancho * (valor/100)
            binding.aguja.translationX = x
        }
    }

    private fun pesoIdeal() : Float{
        var ideal: Float
        var alturacm = altura

        if (medidaAlto == 1)
            alturacm = (altura * 30.48).toFloat()

        if (sexo ==0)
            return 0f
        else {
            if (sexo == 1) {  //Hombre
                ideal = when (alturacm) {
                    in 152f..156f -> 51f
                    in 157f..161f -> 54f
                    in 162f..166f -> 57f
                    in 167f..171f -> 63f
                    in 172f..176f -> 66f
                    in 177f..181f -> 69f
                    in 182f..186f -> 74f
                    in 187f..191f -> 77f
                    in 192f..220f -> 80f
                    else -> 0f
                }
            } else {  // Mujer
                ideal = when (alturacm) {
                    in 152f..156f -> 49f
                    in 157f..161f -> 52f
                    in 162f..166f -> 55f
                    in 167f..171f -> 59f
                    in 172f..176f -> 63f
                    in 177f..181f -> 67f
                    in 182f..186f -> 71f
                    in 187f..191f -> 75f
                    in 192f..220f -> 78f
                    else -> 0f
                }
            }
        }

        if (medidaPeso ==1)
            ideal *= 2.20462f

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

    private fun abrirDatosPersonales(nuevo: Boolean) {
        val intent = Intent(requireContext(), DatosPersonales::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("nuevo", nuevo)
        startActivity(intent)
    }

    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    private fun grabaPreferencias() {
        val sharedPreferences = requireActivity().getSharedPreferences("Preferencias", MODE_PRIVATE)
        sharedPreferences.edit {

            putInt("diaNac", diaNac)
            putInt("mesNac", mesNac)
            putInt("anoNac", anoNac)
            putInt("sexo", sexo)
            putFloat("altura", altura)
            putInt("medidaPeso", medidaPeso)
            putInt("medidaAlto", medidaAlto)
            putString("nombre", nombre)
            putInt("actividad", actividad)
            putString("version", versionApp)
        }
    }

    private fun leerPreferences(){
        val sharedPreferences = requireActivity().getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        val version = getAppVersionCode(binding.fab.context)

        diaNac = sharedPreferences.getInt("diaNac", 0)
        mesNac = sharedPreferences.getInt("mesNac", 0)
        anoNac = sharedPreferences.getInt("anoNac", 0)
        sexo = sharedPreferences.getInt("sexo", 0)
        versionApp = sharedPreferences.getString("version", "") ?: ""
        actividad = sharedPreferences.getInt("actividad", -1)

        try {
            altura = sharedPreferences.getFloat("altura", 0f)
            nombre = sharedPreferences.getString("nombre", "").toString()
        } catch (e: Exception) {
            altura = 0f
            nombre = ""
        }

        if (version > 4) {
            medidaPeso = sharedPreferences.getInt("medidaPeso", 0)
            medidaAlto = sharedPreferences.getInt("medidaAlto", 0)
        } else {
            medidaPeso = 0
            medidaAlto = 0
        }

        val baseDatosSQL = SQLLite(requireContext())
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)
        val datos = baseDatosSQL.Select("")
        baseDatosSQL.cierra(db)

        if (datos.isNotEmpty()) {
            peso = datos[0].peso
            fecha = datos[0].fecha
        }
    }
}