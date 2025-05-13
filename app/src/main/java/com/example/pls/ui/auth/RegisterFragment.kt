package com.example.pls.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pls.R
import com.example.pls.databinding.FragmentRegisterBinding
import com.example.pls.utils.UserSessionManager

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        userSessionManager = UserSessionManager(requireContext())

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                register(name, email, phone, password)
            }
        }

        binding.loginText.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        authViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is AuthViewModel.AuthResult.Success -> {
                        hideLoading()
                        userSessionManager.saveUserSession(it.userSession)
                        navigateToHome()
                    }
                    is AuthViewModel.AuthResult.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                    is AuthViewModel.AuthResult.Loading -> {
                        showLoading()
                    }
                }
            }
        }
    }

    private fun register(name: String, email: String, phone: String, password: String) {
        authViewModel.register(name, email, phone, password)
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameInputLayout.error = "กรุณากรอกชื่อ-นามสกุล"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "กรุณากรอกอีเมล"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "กรุณากรอกรหัสผ่าน"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "กรุณายืนยันรหัสผ่าน"
            isValid = false
        } else if (confirmPassword != password) {
            binding.confirmPasswordInputLayout.error = "รหัสผ่านไม่ตรงกัน"
            isValid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }

        return isValid
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_registerFragment_to_navigation_home)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.registerButton.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.registerButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}