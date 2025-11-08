package com.example.lumanotifier

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private lateinit var deviceSpinner: Spinner
    private lateinit var connectButton: Button
    private lateinit var sendButton: Button
    private lateinit var selectAppsButton: Button
    private lateinit var inputField: EditText
    private lateinit var logView: TextView

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var selectedDevice: BluetoothDevice? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_main)

        findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            Toast.makeText(this, "Connected to Luma", Toast.LENGTH_SHORT).show()
        }

        deviceSpinner = findViewById(R.id.deviceSpinner)
        connectButton = findViewById(R.id.connectButton)
        sendButton = findViewById(R.id.sendButton)
        selectAppsButton = findViewById(R.id.selectAppsButton)
        inputField = findViewById(R.id.inputField)
        logView = findViewById(R.id.logView)

        // Check Bluetooth availability
        if (bluetoothAdapter == null) {
            logView.text = "Bluetooth not supported on this device"
            return
        }

        // Request permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )
        }

        // Populate paired devices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val deviceNames = pairedDevices?.map { it.name } ?: listOf("No paired devices")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View,
                position: Int,
                id: Long
            ) {
                if (pairedDevices != null && pairedDevices.isNotEmpty()) {
                    selectedDevice = pairedDevices.elementAt(position)
                    logView.text = "Selected: ${selectedDevice?.name}\n"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Open app selection
        selectAppsButton.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        // Connect and start background service
        connectButton.setOnClickListener {
            if (selectedDevice == null) {
                logView.append("No device selected\n")
                return@setOnClickListener
            }

            val intent = Intent(this, BluetoothService::class.java)
            intent.putExtra("device_address", selectedDevice!!.address)
            startForegroundService(intent)
            logView.append("Started background service for ${selectedDevice!!.name}\n")
        }

        // Manual message sending
        sendButton.setOnClickListener {
            val msg = inputField.text.toString()
            if (msg.isNotEmpty()) {
                BluetoothLink.send?.invoke(msg)
                logView.append("Sent: $msg\n")
                inputField.text.clear()
            } else {
                logView.append("No device connected\n")
            }
        }
    }
}
