package com.example.deltabot

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
// UI
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    // Robot
    var robot: Bot? = null

    // Bluetooth
    var input : InputStream? = null
    var defaultName = "RNBT-80B9"
    var socket : BluetoothSocket? = null
    var ldevice : BluetoothDevice? = null
    var debug = true
    var bluetoothManager : BluetoothManager? = null
    var bluetooth : BluetoothAdapter? = null
    var receiver : BroadcastReceiver? = null

    // Sequence d'actions a effectuer
    private val tabActions = ArrayList<UserAction>();

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetooth = bluetoothManager!!.adapter as BluetoothAdapter
        // On check si l'appareil dispose du bluetooth
        if (bluetooth == null) {
            logMessage("Pas de bluetooth sur l'appareil")
            finish()
            System.exit(0)
        }
        // On active le bluetooth s'il ne l'est pas
        if (bluetooth!!.isEnabled) {
            logMessage("Bluetooth on")
        } else {
            logMessage("Bluetooth off\nAllumage...")
            bluetooth!!.enable()
        }
        // On arrete le scan d'appareils s'il est en cours
        if (bluetooth!!.isDiscovering()) {
            bluetooth!!.cancelDiscovery()
        }
        var startDiscovery = true
        // Non fonctionnel : Ne pas scanner si le module bluetooth du robot est deja appairé
        /*val devices = bluetooth!!.bondedDevices
        if (devices.size > 0) {
            logMessage("Appareils deja appaires")
            for (currentDevice in devices) {
                if (currentDevice.name == defaultName) {
                    ldevice = currentDevice
                    socket = ldevice!!.createRfcommSocketToServiceRecord(UUID.randomUUID()) // A verifier
                    initPipe()
                    startDiscovery = false
                }
            }
        }*/
        // On commence le scan d'appareils bluetooth
        if (startDiscovery) {
            initReceiver()
            bluetooth!!.startDiscovery()
        }
    }

    // Gestion des evenements bluetooth
    fun initReceiver() {
        receiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                // SWITCH
                when(intent.action) {
                    // Quand appareil découvert
                    BluetoothDevice.ACTION_FOUND -> {
                        // On check si l'appareil découvert est le module bluetooth concerné
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        if (device?.name != null && device.name == defaultName) {
                            ldevice = device
                            bluetooth?.cancelDiscovery()
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        logMessage("Discovery start")
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        logMessage("Discovery ended")
                        if (ldevice != null) {
                            logMessage("Module bluetooth du robot decouvert")
                            ldevice!!.fetchUuidsWithSdp() // Provoque ACTION_UUID (On récupère les uuid du module pour s'appairer)
                        } else {
                            logMessage("Module bluetooth du robot introuvable")
                        }
                    }
                    // A la reception des uuids du module
                    BluetoothDevice.ACTION_UUID -> {
                        ldevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                        // val uuidExtra = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                        if (uuidExtra != null && ldevice!!.name == defaultName) {
                            for (p in uuidExtra) {
                                logMessage(p.toString())
                            }
                            try {
                                socket = ldevice!!.createRfcommSocketToServiceRecord(UUID.fromString(uuidExtra[0].toString()))
                                // socket = ldevice!!.createRfcommSocketToServiceRecord(uuidExtra)
                            } catch (e: Exception) {
                                logMessage("Probleme creation socket\nCLOSING")
                                System.exit(0)
                            }
                            initPipe()
                        } else {
                            logMessage(ldevice!!.name)
                            logMessage("UUID null ou ne correspondant pas au robot")
                        }
                    }
                }
            }
        }
        // Evenements qui nous interessent
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_UUID)
        }
        registerReceiver(receiver, intentFilter)
    }

    // Création des flux de communication avec le module bluetooth
    @SuppressLint("MissingPermission")
    fun initPipe() {
        socket?.let { socketT ->
            socketT.connect()
            logMessage("Liaison etablie")
            // output = socketT.outputStream
            input = socketT.inputStream
            robot = Bot(0,0,0, socketT.outputStream)
            val status: TextView = findViewById(R.id.botStatus);
            status.text = "Bot en ligne"
            status.setTextColor(Color.parseColor("#2AD100"))
        }
    }

    // ---------------------------------
    // Methodes interactions UI
    // ---------------------------------

    public fun addPosition(view: View) {
        val b: Button = view as Button
        val buttonText: String = b.getText().toString()
        tabActions.add(UserAction(0, buttonText));
        this.updateListActions();
    }
    public fun magnetOn(view: View) {
        tabActions.add(UserAction(1, ""));
        this.updateListActions();
    }

    public fun magnetOff(view: View) {
        tabActions.add(UserAction(2, ""));
        this.updateListActions();
    }

    private fun updateListActions() {
        val listActions: ListView = findViewById(R.id.listActions);
        val adapter = ActionAdapter(this, tabActions);
        listActions.adapter = adapter;
    }

    public fun launchActions(view: View) {
        if (!tabActions.isEmpty()) {
            for (action in tabActions) {
                robot!!.processAction(action)
            }
        }
        logMessage(robot!!.a.toString() + "" + robot!!.b.toString() + "" + robot!!.c.toString())
    }

    // ---------------------------------
    // LOGGER (LOGCAT)
    // ---------------------------------
    fun logMessage(str: String) {
        if (debug) {
            Log.i("msgcustom", str) // Filtrer son logcat avec msgcustom
            val logger: TextView = findViewById(R.id.logger);
            logger.text = str;
        }
    }
}