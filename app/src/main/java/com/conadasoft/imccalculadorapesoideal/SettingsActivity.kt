package com.conadasoft.imccalculadorapesoideal

import SettingsFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Si es la primera vez que se crea, añade el fragmento
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }

        // Añade una flecha para volver atrás en la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}