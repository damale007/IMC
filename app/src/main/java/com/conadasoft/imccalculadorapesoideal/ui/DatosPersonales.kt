package com.conadasoft.imccalculadorapesoideal.ui

import SQLLite
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.conadasoft.imccalculadorapesoideal.DatePickerFragment
import com.conadasoft.imccalculadorapesoideal.MainActivity
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.ActivityDatosPersonalesBinding
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.altura
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.anoNac
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.diaNac
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.fecha
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.medidaAlto
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.medidaPeso
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.mesNac
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.peso
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.sexo
import java.util.Calendar


class DatosPersonales : AppCompatActivity() {
    private lateinit var binding: ActivityDatosPersonalesBinding
    var dia:Int = 0
    var mes:Int = 0
    var ano:Int = 0
    var mPeso:Int = 0
    var mAlto:Int = 0
    var inicio_mPeso:Int = 0
    var inicio_mAlto:Int = 0
    var nuevo:Boolean = false
    var nombre: String = ""
    var actividad: Int = -1
    var pesoInicial: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatosPersonalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nuevo = intent.extras?.getBoolean("nuevo", true) ?: true

        binding.fecha.setOnClickListener{ showDatePickerDialog() }

        val sharedPreferences = getSharedPreferences("Preferencias", MODE_PRIVATE)
        inicio_mPeso = sharedPreferences.getInt("medidaPeso", 0)
        inicio_mAlto = sharedPreferences.getInt("medidaAlto", 0)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.m_peso, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.medidasPeso.adapter = adapter

        val adapter2 = ArrayAdapter.createFromResource(
            this,
            R.array.m_alto, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.medidasAlto.adapter = adapter2

        val adapter3 = ArrayAdapter.createFromResource(
            this,
            R.array.m_actividad, android.R.layout.simple_spinner_item
        )
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.actividad.adapter = adapter3

        binding.tituloAltoP.visibility = (View.GONE)
        binding.tituloAltoPP.visibility = (View.GONE)
        binding.idAltura2.visibility = (View.GONE)


        if (!nuevo) {
            rellenaTarjeta()
        }

        binding.medidasAlto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    binding.tituloAltoP.visibility = (View.GONE)
                    binding.tituloAltoPP.visibility = (View.GONE)
                    binding.idAltura2.visibility = (View.GONE)
                } else {
                    binding.tituloAltoP.visibility = (View.VISIBLE)
                    binding.tituloAltoPP.visibility = (View.VISIBLE)
                    binding.idAltura2.visibility = (View.VISIBLE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.botonGrabar.setOnClickListener {
            var error = false;

            if (binding.idPeso.text.isEmpty()){
                error = true
                dialogo(resources.getString(R.string.atencion), resources.getString(R.string.errorPeso))
            }

            if (binding.idAltura.text.isEmpty()) {
                error = true
                dialogo(
                    resources.getString(R.string.atencion),
                    resources.getString(R.string.errorAltura)
                )
            }

            if (binding.idPeso.text.isEmpty()) {
                error = true
                dialogo(
                    resources.getString(R.string.atencion), resources.getString(R.string.errorPeso)
                )
            }

            if (binding.idNombre.text.isEmpty()) {
                error = true
                dialogo(
                    resources.getString(R.string.atencion), resources.getString(R.string.errorNombre)
                )
            }

            if (!error)
                grabaDatos()
        }
    }

    private fun rellenaTarjeta() {
        val sharedPreferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)

        diaNac = sharedPreferences.getInt("diaNac", 0)
        mesNac = sharedPreferences.getInt("mesNac", 0)
        anoNac = sharedPreferences.getInt("anoNac", 0)
        sexo = sharedPreferences.getInt("sexo", 0)
        actividad = sharedPreferences.getInt("actividad", -1)

        dia = diaNac
        mes = mesNac
        ano = anoNac
        try {
            altura = sharedPreferences.getFloat("altura", 0f)
            nombre = sharedPreferences.getString("nombre", "").toString()
        } catch (e: Exception) {
            altura = 0f
            nombre = ""
        }

        medidaPeso = sharedPreferences.getInt("medidaPeso", 0)
        medidaAlto = sharedPreferences.getInt("medidaAlto", 0)

        val baseDatosSQL = SQLLite(this)
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)
        val datos = baseDatosSQL.Select("")
        baseDatosSQL.cierra(db)

        if (datos.size > 0) {
            peso = datos[0].peso
            pesoInicial = peso
            fecha = datos[0].fecha
        }
        binding.fechaNacimentoTexto.setText("$diaNac/$mesNac/$anoNac")
        binding.idPeso.setText(peso.toString())
        binding.idNombre.setText(nombre)
        binding.medidasPeso.setSelection(medidaPeso)
        binding.medidasAlto.setSelection(medidaAlto)
        if (medidaAlto == 1)
            binding.idAltura2.setText(altura.toString())
        binding.idAltura.setText(altura.toString())
        if (sexo == 1)
            binding.radioMasc.isChecked = true
        if (sexo == 2)
            binding.radioFem.isChecked = true

        if (actividad != -1)
            binding.actividad.setSelection(actividad)
    }

    private fun grabaDatos() {
        val topePeso = arrayOf(150f, 330f)
        val topeAltura = arrayOf(50f, 1.6f)
        mPeso = binding.medidasPeso.selectedItemPosition
        mAlto = binding.medidasAlto.selectedItemPosition
        actividad = binding.actividad.selectedItemPosition

        val peso = binding.idPeso.text.toString().toFloat()
        var altura = binding.idAltura.text.toString().toFloat()
        if (mAlto == 1)
            altura += binding.idAltura2.text.toString().toFloat() / 12f
        var sexo = 0
        if (binding.radioMasc.isChecked)
            sexo = 1
        if (binding.radioFem.isChecked)
            sexo = 2

        if (dia == 0 || mes == 0 || ano == 0 || peso == 0f || altura < topeAltura[mAlto] || peso > topePeso[mPeso]) {
            dialogo(resources.getString(R.string.atencion), resources.getString(R.string.errorDatos))
        } else {
            diaNac = dia
            mesNac = mes
            anoNac = ano
            InicioFragment.sexo = sexo
            InicioFragment.altura = altura
            InicioFragment.peso = peso
            InicioFragment.nombre = binding.idNombre.text.toString()
            InicioFragment.actividad = actividad

            grabaConfig()

            if (pesoInicial != peso)
                actualizaBD()

            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else this.finish()
        }
    }

    private fun actualizaBD() {
        //Actualiza base de datos
        val baseDatosSQL = SQLLite(this)
        val db = baseDatosSQL.writableDatabase
        baseDatosSQL.inicia(db)

        val c: Calendar = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH) + 1
        val year = c.get(Calendar.YEAR)

        if (isTaskRoot)
            baseDatosSQL.insertaDiario("$year-$month-$day", InicioFragment.peso)
        else {
            //Actualiza los pesos de la base de datos si hay cambio de medidas
            //Modifica el primer registro con el nuevo peso
            var newPeso: Float
            var peso: Float

            if (mPeso != inicio_mPeso) {
                val pesos = baseDatosSQL.Select("")

                for (dato in pesos) {
                    peso = dato.peso
                    val idSt = dato.id

                    if (mPeso == 0) {
                        //Convierte de libras a kilos
                        newPeso = peso / 2.20462f
                    } else {
                        // Convierte de kilos a libras
                        newPeso = peso * 2.20462f
                    }
                    baseDatosSQL.actualizaDiario(idSt.toInt(), newPeso)
                }
            }
        }
        baseDatosSQL.actualizaDiario(1, InicioFragment.peso)
        baseDatosSQL.cierra(db)
    }

    private fun grabaConfig() {
        val sharedPreferences = getSharedPreferences("Preferencias", MODE_PRIVATE)
        sharedPreferences.edit {

            putInt("diaNac", dia)
            putInt("mesNac", mes)
            putInt("anoNac", ano)
            putInt("sexo", InicioFragment.sexo)
            putFloat("altura", InicioFragment.altura)
            putInt("medidaPeso", mPeso)
            putInt("medidaAlto", mAlto)
            putString("nombre", InicioFragment.nombre)
            putInt("actividad", actividad)
        }
    }

    private fun dialogo(titulo: String, texto: String) {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(titulo)
        builder.setMessage(texto)
        builder.setPositiveButton(R.string.ok) { dialog, which ->
            dialog.cancel()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePciker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int) {
        binding.fechaNacimentoTexto.setText("$day/$month/$year")
        dia = day
        mes = month +1
        ano = year
    }
}