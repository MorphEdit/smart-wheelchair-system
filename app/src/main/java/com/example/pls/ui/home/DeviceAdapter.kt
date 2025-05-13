package com.example.pls.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.pls.R
import com.example.pls.databinding.ItemDeviceBinding
import com.example.pls.model.WheelchairResponse
import java.text.SimpleDateFormat
import java.util.*

class DeviceAdapter(private val devices: List<WheelchairResponse>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: WheelchairResponse) {
            binding.deviceName.text = device.name

            // กำหนดสถานะตามข้อมูลจาก API
            val isOnline = device.status == "active"
            binding.deviceStatus.text = if (isOnline) "ออนไลน์" else "ออฟไลน์"
            binding.deviceStatus.setTextColor(
                binding.root.context.getColor(
                    if (isOnline) R.color.colorSuccess else R.color.colorAccent
                )
            )

            // จำลองข้อมูลแบตเตอรี่ (ถ้า API ไม่มีข้อมูลนี้)
            binding.batteryText.text = "85%"

            // จำลองความแรงของสัญญาณ
            binding.signalText.text = if (isOnline) "ดี" else "ไม่มี"

            // ตั้งค่าปุ่มควบคุม
            binding.btnControl.setOnClickListener {
                // นำทางไปยังหน้า Dashboard พร้อมส่งข้อมูล ID ของอุปกรณ์
                val navController = binding.root.findNavController()
                val bundle = androidx.core.os.bundleOf("wheelchairId" to device.id)
                navController.navigate(R.id.navigation_dashboard, bundle)
            }
        }
    }
}