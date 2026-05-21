package com.example.appalmacen.view.activities

import android.os.Bundle
import android.widget.Toast
import com.example.appalmacen.databinding.ActivityConfiguracionBinding
import com.example.appalmacen.utils.PreferencesManager

class ConfiguracionActivity : BaseActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private lateinit var prefManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarConfig)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarConfig.setNavigationOnClickListener { finish() }

        prefManager = PreferencesManager(this)
        
        // Cargar valor actual (en minutos)
        val currentTimeoutMillis = prefManager.getLogoutTimeout()
        binding.etLogoutTime.setText((currentTimeoutMillis / 60000).toString())

        binding.btnSaveConfig.setOnClickListener {
            guardarConfig()
        }
    }

    private fun guardarConfig() {
        val minStr = binding.etLogoutTime.text.toString()
        val minutos = minStr.toLongOrNull() ?: 10
        
        if (minutos < 1) {
            Toast.makeText(this, "El tiempo mínimo es 1 minuto", Toast.LENGTH_SHORT).show()
            return
        }

        prefManager.saveLogoutTimeout(minutos * 60000)
        Toast.makeText(this, "Configuración guardada. Se aplicará al reiniciar.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
