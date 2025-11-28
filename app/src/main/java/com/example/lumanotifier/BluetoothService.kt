package com.example.lumanotifier

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.OutputStream
import java.util.UUID

object BluetoothHelper {
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    interface BluetoothListener {
        fun onLog(message: String)
        fun onConnected()
        fun onDisconnected()
        fun onError(message: String)
    }

    var listener: BluetoothListener? = null

    fun connect(device: BluetoothDevice, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (adapter == null) {
            onError("Bluetooth not supported")
            listener?.onError("Bluetooth not supported")
            return
        }

        Thread {
            try {
                listener?.onLog("Connecting to ${device.name ?: device.address}...")
                // Cancel discovery because it otherwise slows down the connection.
                adapter.cancelDiscovery()
                
                val tmpSocket = device.createRfcommSocketToServiceRecord(uuid)
                tmpSocket.connect()
                socket = tmpSocket
                outputStream = tmpSocket.outputStream

                BluetoothLink.send = { msg ->
                    send(msg)
                }

                listener?.onLog("Connected to ${device.name ?: device.address}")
                listener?.onConnected()
                onSuccess()
            } catch (e: Exception) {
                Log.e("BluetoothHelper", "Connection failed", e)
                val msg = "Connection failed: ${e.message}"
                listener?.onError(msg)
                listener?.onLog(msg)
                onError(msg)
                disconnect()
            }
        }.start()
    }

    fun send(msg: String) {
        try {
            outputStream?.write((msg + "\n").toByteArray())
            listener?.onLog("Sent: $msg")
        } catch (e: Exception) {
            Log.e("BluetoothHelper", "Send failed", e)
            listener?.onError("Send failed: ${e.message}")
        }
    }

    fun disconnect() {
        try { outputStream?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        outputStream = null
        BluetoothLink.send = null
        listener?.onDisconnected()
        listener?.onLog("Disconnected")
    }
}
