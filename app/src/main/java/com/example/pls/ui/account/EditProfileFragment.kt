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
import com.example.pls.databinding.FragmentEditProfileBinding
import com.example.pls.utils.UserSessionManager
import com.google.android.material.snackbar.Snackbar

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AccountViewModel
    private lateinit var userSessionManager: UserSessionManager
    private val TAG = "EditProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userSessionManager = UserSessionManager(requireContext())

        // ตรวจสอบว่าผู้ใช้เข้าสู่ระบบหรือยัง
        if (!userSessionManager.isLoggedIn()) {
            navigateToLogin()
            return root
        }

        // ตั้งค่าข้อมูลเริ่มต้น
        setupInitialData()

        // ตั้งค่าปุ่มกด
        setupButtons()

        // สังเกตการแสดงผลขณะโหลด
        observeLoadingState()

        return root
    }

    private fun setupInitialData() {
        // ดึงข้อมูลผู้ใช้ปัจจุบันจาก UserSessionManager โดยตรง
        val user = userSessionManager.getUser()
        if (user != null) {
            binding.editTextName.setText(user.name)
            binding.editTextEmail.setText(user.email ?: "")
            // แก้ไข: ตรวจสอบค่าเบอร์โทรศัพท์ก่อนกำหนดค่า
            binding.editTextPhone.setText(user.phone ?: "")

            Log.d(TAG, "โหลดข้อมูลผู้ใช้: ${user.name}, ${user.email}, ${user.phone}")
        } else {
            Log.e(TAG, "ไม่พบข้อมูลผู้ใช้ในระบบ")
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }
    }

    private fun setupButtons() {
        // ปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // ปุ่มเปลี่ยนรูปโปรไฟล์
        binding.btnChangeProfilePic.setOnClickListener {
            Toast.makeText(requireContext(), "ฟีเจอร์นี้ยังไม่พร้อมใช้งาน", Toast.LENGTH_SHORT).show()
        }

        // ปุ่มบันทึก
        binding.btnSave.setOnClickListener {
            // ตรวจสอบข้อมูลที่กรอกและบันทึก
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val phone = binding.editTextPhone.text.toString().trim()

            if (name.isEmpty()) {
                binding.editTextName.error = "กรุณากรอกชื่อ"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.editTextEmail.error = "กรุณากรอกอีเมล"
                return@setOnClickListener
            }

            // ส่งข้อมูลไปอัปเดตโดยใช้ API จริง
            updateProfileOnServer(name, email, phone)
        }
    }

    private fun updateProfileOnServer(name: String, email: String, phone: String) {
        // แสดงว่ากำลังโหลด
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        // เรียกใช้ viewModel เพื่ออัปเดตข้อมูลผ่าน API
        viewModel.updateProfile(name, email, phone,
            onSuccess = {
                // แจ้งเตือนว่าบันทึกสำเร็จ
                Toast.makeText(requireContext(), "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                // กลับไปยังหน้าโปรไฟล์
                findNavController().navigateUp()
            },
            onError = { errorMessage ->
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                Log.e(TAG, "เกิดข้อผิดพลาดในการบันทึกข้อมูล: $errorMessage")
            }
        )
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_editProfileFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}