package com.example.pls.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pls.R
import com.example.pls.databinding.FragmentSettingsBinding
import com.example.pls.databinding.SettingItemBinding
import com.example.pls.utils.UserSessionManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // เริ่มต้น UserSessionManager
        sessionManager = UserSessionManager(requireContext())

        // เซ็ต RecyclerView
        setupRecyclerView(settingsViewModel)

        // เซ็ตปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return root
    }

    private fun setupRecyclerView(viewModel: SettingsViewModel) {
        binding.settingsList.layoutManager = LinearLayoutManager(requireContext())

        // สังเกตการเปลี่ยนแปลงข้อมูลและอัปเดต adapter
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            binding.settingsList.adapter = SettingsAdapter(settings) { settingItem ->
                handleSettingClick(settingItem)
            }
        }
    }

    private fun handleSettingClick(setting: SettingItem) {
        when (setting.id) {
            "pair_wheelchair" -> {
                // ตรวจสอบว่าล็อกอินแล้วหรือไม่
                if (sessionManager.isLoggedIn()) {
                    // นำทางไปที่หน้าจับคู่
                    findNavController().navigate(R.id.action_navigation_settings_to_pairingFragment)
                } else {
                    // ถ้ายังไม่ได้ล็อกอิน ให้ไปหน้าล็อกอิน
                    findNavController().navigate(R.id.action_navigation_settings_to_loginFragment)
                }
            }
            // สามารถเพิ่มการจัดการการคลิกสำหรับรายการอื่นๆ ได้ที่นี่
            else -> {
                // กรณีอื่นๆ ทำอะไรก็ได้ หรือไม่ทำอะไร
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter สำหรับ RecyclerView
    private inner class SettingsAdapter(
        private val settings: List<SettingItem>,
        private val onItemClick: (SettingItem) -> Unit
    ) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = SettingItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val setting = settings[position]
            holder.bind(setting)
        }

        override fun getItemCount() = settings.size

        inner class ViewHolder(private val binding: SettingItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(setting: SettingItem) {
                binding.settingTitle.text = setting.title
                binding.settingDescription.text = setting.description

                // เพิ่ม event click
                binding.root.setOnClickListener {
                    onItemClick(setting)
                }
            }
        }
    }
}