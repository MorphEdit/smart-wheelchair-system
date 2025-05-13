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
import com.example.pls.databinding.FragmentLoginBinding
import com.example.pls.utils.UserSessionManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        userSessionManager = UserSessionManager(requireContext())

        // ตรวจสอบว่าผู้ใช้เข้าสู่ระบบแล้วหรือไม่
        if (userSessionManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(email, password)) {
                login(email, password)
            }
        }

        binding.registerText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
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

    private fun login(email: String, password: String) {
        authViewModel.login(email, password)
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "กรุณากรอกอีเมล"
            return false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "กรุณากรอกรหัสผ่าน"
            return false
        }

        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        return true
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}