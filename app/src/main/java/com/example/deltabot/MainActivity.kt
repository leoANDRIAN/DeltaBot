package com.example.deltabot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetooth = bluetoothManager.adapter as BluetoothAdapter
        if (bluetooth == null) {
            updateLog("L'appareil ne supporte pas le bluetooth")
        }
        updateLog("Tout va bien 2")

    }

    fun updateLog(str: String) {
        val logView = findViewById<TextView>(R.id.log)
        logView.text = str
    }
}