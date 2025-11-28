package com.example.lumanotifier

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    private var selectedPosition = -1

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
        val statusChip: Chip = itemView.findViewById(R.id.statusChip)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onDeviceClick(devices[position])
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice, isSelected: Boolean) {
            try {
                deviceName.text = device.name ?: "Unknown Device"
                deviceAddress.text = device.address ?: "N/A"
                statusChip.text = if (isSelected) "Selected" else "Paired"
                
                // Update card appearance based on selection
                itemView.isSelected = isSelected
            } catch (e: SecurityException) {
                deviceName.text = "Permission Required"
                deviceAddress.text = "Grant Bluetooth permissions"
                statusChip.text = "Error"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], position == selectedPosition)
    }

    override fun getItemCount() = devices.size

    fun getSelectedDevice(): BluetoothDevice? {
        return if (selectedPosition >= 0 && selectedPosition < devices.size) {
            devices[selectedPosition]
        } else null
    }
}
