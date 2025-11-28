package com.example.lumanotifier
import com.example.lumanotifier.databinding.ActivityMainBinding

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import android.provider.Settings
import android.content.ComponentName
import android.app.AlertDialog
import android.service.notification.NotificationListenerService

class MainActivity : AppCompatActivity() {

    private lateinit var deviceSpinner: Spinner
    private lateinit var binding: ActivityMainBinding
    private lateinit var connectButton: Button
    private lateinit var scanButton: Button
    private lateinit var sendButton: Button
    private lateinit var selectAppsButton: Button
    private lateinit var inputField: EditText
    private lateinit var logView: TextView
    private lateinit var logScrollView: ScrollView

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }
    private var selectedDevice: BluetoothDevice? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Connected to BPager", Toast.LENGTH_SHORT).show()
        }

        deviceSpinner = binding.deviceSpinner
        connectButton = binding.connectButton
        scanButton = binding.scanButton
        sendButton = binding.sendButton
        selectAppsButton = binding.selectAppsButton
        inputField = binding.inputField
        logView = binding.logView
        logScrollView = binding.logScrollView

        // Check Bluetooth support
        if (bluetoothAdapter == null) {
            setLog("Bluetooth not supported on this device")
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
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )
        }

        // Ask user to grant access if not enabled
        val cn = ComponentName(this, NotificationForwarderService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")

        if (flat == null || !flat.contains(cn.flattenToString())) {
            AlertDialog.Builder(this)
                .setTitle("Notification Access")
                .setMessage("Please enable notification access for BPager to forward app notifications.")
                .setPositiveButton("Grant Access") { _, _ ->
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Show paired devices initially
        refreshPairedDevices()

        // Wire BluetoothHelper listener
        BluetoothHelper.listener = object : BluetoothHelper.BluetoothListener {
            override fun onLog(message: String) {
                runOnUiThread { appendLog("$message\n") }
            }

            override fun onConnected() {
                runOnUiThread { 
                    appendLog("Connected successfully\n") 
                    Toast.makeText(this@MainActivity, "Connected", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDisconnected() {
                runOnUiThread { appendLog("Disconnected\n") }
            }

            override fun onError(message: String) {
                runOnUiThread { appendLog("Error: $message\n") }
            }
        }

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                 // No-op, just holding selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Scan button now just refreshes paired devices and opens system settings
        scanButton.setOnClickListener { 
            refreshPairedDevices()
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Pair device in Settings, then refresh list", Toast.LENGTH_LONG).show()
        }

        // Open app selection screen
        selectAppsButton.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        // Connect
        connectButton.setOnClickListener {
            val deviceName = deviceSpinner.selectedItem as? String
            if (deviceName == null) {
                appendLog("No device selected\n")
                return@setOnClickListener
            }
            
            // Extract address from "Name (Address)" string
            // Assuming format: "DeviceName (00:11:22:33:44:55)"
            val address = deviceName.substringAfterLast("(").substringBeforeLast(")")
            
            try {
                val device = bluetoothAdapter?.getRemoteDevice(address)
                if (device != null) {
                    appendLog("Connecting to $address...\n")
                    BluetoothHelper.connect(device, onSuccess = {}, onError = {})
                } else {
                    appendLog("Invalid device address\n")
                }
            } catch (e: Exception) {
                appendLog("Error getting device: ${e.message}\n")
            }
        }

        // Manual test message sender
        sendButton.setOnClickListener {
            val msg = inputField.text.toString().trim()
            if (msg.isNotEmpty()) {
                BluetoothHelper.send(msg)
                inputField.text?.clear()
            } else {
                appendLog("No message entered\n")
            }
        }
    }

    private fun refreshPairedDevices() {
        val bonded = try {
            bluetoothAdapter?.bondedDevices
        } catch (e: SecurityException) {
            setLog("Permission missing to list devices")
            null
        }

        val deviceNames = mutableListOf<String>()
        bonded?.forEach { d ->
            deviceNames.add("${d.name ?: "Unknown"} (${d.address})")
        }
        
        if (deviceNames.isEmpty()) {
            deviceNames.add("No paired devices found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter
    }

    override fun onDestroy() {
            super.onDestroy()
            BluetoothHelper.disconnect()
    }

    // Helper to set the log text (clears previous) and scroll to bottom
    private fun setLog(text: String) {
        runOnUiThread {
            logView.text = text + "\n"
            logScrollView.post { logScrollView.fullScroll(android.view.View.FOCUS_DOWN) }
        }
    }

    // Helper to append a log message and scroll to bottom
    private fun appendLog(text: String) {
        logView.append(text)
        logScrollView.post { logScrollView.fullScroll(android.view.View.FOCUS_DOWN) }
    }
}
