package com.example.pls.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pls.R
import com.example.pls.databinding.FragmentHomeBinding
import com.example.pls.utils.UserSessionManager
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        userSessionManager = UserSessionManager(requireContext())

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()

        // โหลดข้อมูลเมื่อเปิดหน้า
        homeViewModel.loadWheelchairs()
    }

    private fun setupUI() {
        // ตั้งค่า RecyclerView
        binding.devicesRecycler.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )

        // ตั้งค่าชื่อผู้ใช้จากข้อมูลจริง
        val user = userSessionManager.getUser()
        binding.userName.text = user?.name ?: "คุณผู้ใช้"

        // ตั้งค่าข้อความต้อนรับตามเวลาของวัน
        binding.welcomeText.text = getGreetingMessage()

        // ตั้งค่าปุ่มการดำเนินการด่วน
        setupQuickActions()
    }

    private fun setupQuickActions() {
        // ปุ่มจับคู่อุปกรณ์
        binding.btnPairDevice.setOnClickListener {
            navigateToPairing()
        }

        // ปุ่มควบคุม
        binding.btnControl.setOnClickListener {
            // ถ้ามีการจับคู่กับรถวีลแชร์แล้ว ให้ไปที่หน้าควบคุม
            if (userSessionManager.hasPairedWheelchair()) {
                navigateToDashboard()
            } else {
                Toast.makeText(context, "กรุณาจับคู่กับรถวีลแชร์ก่อน", Toast.LENGTH_SHORT).show()
                navigateToPairing()
            }
        }

        // ปุ่มตั้งค่า
        binding.btnQuickSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun setupObservers() {
        // สังเกตการเปลี่ยนแปลงข้อมูลรถวีลแชร์
        homeViewModel.wheelchairs.observe(viewLifecycleOwner) { wheelchairs ->
            // อัปเดต UI ด้วยข้อมูลรถวีลแชร์
            binding.devicesTitle.text = "อุปกรณ์ของคุณ (${wheelchairs.size})"

            // แสดงรายการอุปกรณ์ด้วย RecyclerView
            if (wheelchairs.isNotEmpty()) {
                binding.devicesRecycler.adapter = DeviceAdapter(wheelchairs)
                binding.devicesRecycler.visibility = View.VISIBLE
            } else {
                binding.devicesRecycler.visibility = View.GONE
                // อาจแสดงข้อความว่าไม่มีอุปกรณ์
            }
        }

        // สังเกตสถานะการโหลด
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // แสดงหรือซ่อน indicator การโหลด
            // ถ้ามี ProgressBar ให้ uncomment บรรทัดนี้
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // สังเกตข้อผิดพลาด
        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        return when {
            hourOfDay in 5..11 -> "สวัสดีตอนเช้า"
            hourOfDay in 12..16 -> "สวัสดีตอนบ่าย"
            hourOfDay in 17..20 -> "สวัสดีตอนเย็น"
            else -> "สวัสดีตอนกลางคืน"
        }
    }

    private fun navigateToPairing() {
        findNavController().navigate(R.id.action_navigation_home_to_pairingFragment)
    }

    private fun navigateToDashboard() {
        findNavController().navigate(R.id.navigation_dashboard)
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.navigation_settings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}