package com.example.lumanotifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.OutputStream
import java.util.*

class BluetoothService : Service() {
    private val channelId = "LumaNotifierService"
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val reconnectDelay = 5000L // 5 seconds

    private var deviceAddress: String? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isRunning = true
    private val handler = Handler()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("LumaNotifier Running")
            .setContentText("Forwarding notifications to ESP")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceAddress = intent?.getStringExtra("device_address")
        deviceAddress?.let { connectToBluetooth(it) }
        return START_STICKY
    }

    private fun connectToBluetooth(address: String) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val device: BluetoothDevice = adapter.getRemoteDevice(address)

        Thread {
            while (isRunning) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    adapter.cancelDiscovery()
                    bluetoothSocket!!.connect()
                    outputStream = bluetoothSocket!!.outputStream

                    Log.d("BluetoothService", "Connected to $address")
                    BluetoothLink.send = { msg ->
                        try {
                            outputStream?.write("$msg\n".toByteArray())
                        } catch (e: Exception) {
                            Log.e("BluetoothService", "Write failed: ${e.message}")
                        }
                    }

                    // Block until disconnected
                    while (isRunning && bluetoothSocket?.isConnected == true) {
                        Thread.sleep(1000)
                    }

                } catch (e: Exception) {
                    Log.e("BluetoothService", "Connection failed: ${e.message}")
                }

                // Wait and retry
                if (isRunning) {
                    Log.d("BluetoothService", "Reconnecting in ${reconnectDelay / 1000}s...")
                    Thread.sleep(reconnectDelay)
                }
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Luma Notifier Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
