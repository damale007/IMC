package com.conadasoft.imccalculadorapesoideal.ui

import SQLLite
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.conadasoft.imccalculadorapesoideal.DatePickerFragment
import com.conadasoft.imccalculadorapesoideal.MainActivity
import com.conadasoft.imccalculadorapesoideal.databinding.ActivityDatosPersonalesBinding
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.anoNac
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.diaNac
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.mesNac
import java.util.Calendar

class DatosPersonales : AppCompatActivity() {
    private lateinit var binding: ActivityDatosPersonalesBinding
    var dia:Int = 0
    var mes:Int = 0
    var ano:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatosPersonalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fecha.setOnClickListener{ showDatePickerDialog() }

        binding.botonGrabar.setOnClickListener {
            val peso = binding.idPeso.text.toString().toFloat()
            val altura = binding.idAltura.text.toString().toInt()
            var sexo =0
            if (binding.radioMasc.isChecked)
                sexo = 1
            if (binding.radioFem.isChecked)
                sexo = 2

            if (dia ==0 || mes == 0 || ano ==0 || peso == 0f || altura < 50 || peso >150 ) {
                val builder = AlertDialog.Builder(binding.root.context)
                builder.setTitle("Atención")
                builder.setMessage("Debe rellenar todos los campos con valores correctos")
                builder.setPositiveButton("Aceptar") {dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                val sharedPreferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                diaNac = dia
                mesNac = mes
                anoNac = ano
                InicioFragment.sexo = sexo
                InicioFragment.altura = altura
                InicioFragment.peso = peso

                editor.putInt("diaNac", dia)
                editor.putInt("mesNac", mes)
                editor.putInt("anoNac", ano)
                editor.putInt("sexo", sexo)
                editor.putFloat("peso", peso)
                editor.putInt("altura", altura)
                editor.apply()

                val baseDatosSQL = SQLLite(this)
                val db = baseDatosSQL.writableDatabase
                baseDatosSQL.inicia(db)

                val c: Calendar = Calendar.getInstance()
                val day = c.get(Calendar.DAY_OF_MONTH)
                val month = c.get(Calendar.MONTH) +1
                val year = c.get(Calendar.YEAR)

                baseDatosSQL.insertaDiario("$year-$month-$day", peso)
                baseDatosSQL.cierra(db)

                if (isTaskRoot) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else this.finish()
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
        datePicker.show(supportFragmentManager, "datePciker")
    }

    fun onDateSelected(day: Int, month: Int, year: Int) {
        binding.fechaNacimentoTexto.setText("$day/$month/$year")
        dia = day
        mes = month +1
        ano = year
    }
}