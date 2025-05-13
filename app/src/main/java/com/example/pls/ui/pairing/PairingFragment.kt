package com.example.pls.ui.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pls.R
import com.example.pls.databinding.FragmentPairingBinding
import com.example.pls.utils.UserSessionManager

class PairingFragment : Fragment() {

    private var _binding: FragmentPairingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PairingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPairingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PairingViewModel::class.java)

        // ตรวจสอบว่าล็อกอินแล้วหรือไม่
        val sessionManager = UserSessionManager(requireContext())
        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_pairingFragment_to_loginFragment)
            return
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.pairingState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is PairingViewModel.PairingState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.pairButton.isEnabled = false
                }
                is PairingViewModel.PairingState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.pairButton.isEnabled = true

                    // บันทึกข้อมูลการจับคู่
                    val sessionManager = UserSessionManager(requireContext())
                    sessionManager.savePairedWheelchair(
                        state.pairingData.wheelchairId,
                        state.pairingData.id
                    )

                    Toast.makeText(context, "จับคู่สำเร็จ!", Toast.LENGTH_SHORT).show()

                    // นำไปยังหน้าควบคุมรถวีลแชร์
                    findNavController().navigate(R.id.action_pairingFragment_to_navigation_dashboard)
                }
                is PairingViewModel.PairingState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.pairButton.isEnabled = true
                    Toast.makeText(context, "เกิดข้อผิดพลาด: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.pairButton.setOnClickListener {
            val pairingCode = binding.pairingCodeEditText.text.toString().trim()

            if (pairingCode.length != 6) {
                binding.pairingCodeLayout.error = "กรุณาป้อนรหัสจับคู่ 6 หลัก"
                return@setOnClickListener
            }

            // ดึง userId จาก SessionManager
            val sessionManager = UserSessionManager(requireContext())
            val userId = sessionManager.getUserIdFromToken()

            if (!userId.isNullOrEmpty()) {
                binding.pairingCodeLayout.error = null
                viewModel.pairWithCode(userId, pairingCode)
            } else {
                Toast.makeText(context, "ไม่พบข้อมูลผู้ใช้ กรุณาเข้าสู่ระบบอีกครั้ง", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_pairingFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}