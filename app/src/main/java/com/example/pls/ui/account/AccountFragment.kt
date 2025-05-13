package com.example.pls.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pls.R
import com.example.pls.databinding.FragmentAccountBinding
import com.example.pls.utils.UserSessionManager
import com.google.android.material.snackbar.Snackbar

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSessionManager: UserSessionManager
    private lateinit var viewModel: AccountViewModel
    private val TAG = "AccountFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // สร้าง ViewModel ด้วย ViewModelProvider
        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userSessionManager = UserSessionManager(requireContext())

        // ตรวจสอบว่าผู้ใช้เข้าสู่ระบบหรือยัง
        if (!userSessionManager.isLoggedIn()) {
            Log.d(TAG, "ผู้ใช้ยังไม่ได้เข้าสู่ระบบ")
            navigateToLogin()
            return root
        }

        // แสดงข้อมูลการเข้าสู่ระบบ
        val user = userSessionManager.getUser()
        val token = userSessionManager.getToken()
        Log.d(TAG, "ผู้ใช้เข้าสู่ระบบแล้ว: ${user?.email}, Token: $token")

        // ตั้งค่าการแสดงข้อมูลผู้ใช้
        setupUserProfile()

        // ตั้งค่าปุ่มกด
        setupButtons()

        // สังเกตความเปลี่ยนแปลงของ loading state
        observeLoadingState()

        // สังเกตความเปลี่ยนแปลงของข้อความแจ้งเตือนข้อผิดพลาด
        observeErrorMessages()

        return root
    }

    private fun setupUserProfile() {
        // แสดงข้อมูลจาก ViewModel ผ่าน LiveData
        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.emailText.text = email
        }

        // แสดงชื่อผู้ใช้
        viewModel.name.observe(viewLifecycleOwner) { name ->
            binding.nameText.text = name
        }

        // แสดงเบอร์โทรศัพท์
        viewModel.phone.observe(viewLifecycleOwner) { phone ->
            binding.phoneText.text = phone
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // แสดง/ซ่อน progress indicator ถ้ามี
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun observeErrorMessages() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Log.e(TAG, "พบข้อผิดพลาด: $it")
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun setupButtons() {
        // ปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // ปุ่มตั้งค่า
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_navigation_settings)
        }

        // ปุ่มแก้ไขโปรไฟล์
        binding.btnEditProfile.setOnClickListener {
            // นำทางไปยังหน้าแก้ไขโปรไฟล์
            findNavController().navigate(R.id.action_navigation_account_to_editProfileFragment)
        }

        // ปุ่มออกจากระบบ
        binding.btnLogout.setOnClickListener {
            userSessionManager.logout()
            Toast.makeText(requireContext(), "ออกจากระบบสำเร็จ", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_navigation_account_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}