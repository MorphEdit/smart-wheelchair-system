package com.example.pls.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pls.R
import com.example.pls.databinding.FragmentDashboardBinding
import com.example.pls.utils.UserSessionManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        sessionManager = UserSessionManager(requireContext())

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ตรวจสอบว่าผู้ใช้ล็อกอินแล้วหรือไม่
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "กรุณาเข้าสู่ระบบก่อนใช้งาน", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_navigation_dashboard_to_loginFragment)
            return
        }

        // ตรวจสอบว่ามีรถวีลแชร์ที่จับคู่แล้วหรือไม่
        val pairedWheelchairId = sessionManager.getPairedWheelchairId()
        if (pairedWheelchairId.isNullOrEmpty()) {
            Toast.makeText(context, "กรุณาจับคู่กับรถวีลแชร์ก่อนใช้งาน", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_navigation_dashboard_to_pairingFragment)
            return
        }

        // เพิ่มปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupObservers()
        setupButtons(pairedWheelchairId)
    }

    private fun setupObservers() {
        // สังเกตผลลัพธ์จากการส่งคำสั่ง
        dashboardViewModel.commandResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
        }

        // สังเกตสถานะการโหลด
        dashboardViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // แสดงหรือซ่อนตัวแสดงสถานะการโหลด
            binding.apply {
                btnUp.isEnabled = !isLoading
                btnDown.isEnabled = !isLoading
                btnLeft.isEnabled = !isLoading
                btnRight.isEnabled = !isLoading
                btnCenter.isEnabled = !isLoading
            }
        }

        // สังเกตข้อผิดพลาด
        dashboardViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButtons(wheelchairId: String) {
        binding.apply {
            btnUp.setOnClickListener {
                dashboardViewModel.controlWheelchair(wheelchairId, "up")
            }

            btnDown.setOnClickListener {
                dashboardViewModel.controlWheelchair(wheelchairId, "down")
            }

            btnLeft.setOnClickListener {
                dashboardViewModel.controlWheelchair(wheelchairId, "left")
            }

            btnRight.setOnClickListener {
                dashboardViewModel.controlWheelchair(wheelchairId, "right")
            }

            btnCenter.setOnClickListener {
                dashboardViewModel.controlWheelchair(wheelchairId, "stop")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}