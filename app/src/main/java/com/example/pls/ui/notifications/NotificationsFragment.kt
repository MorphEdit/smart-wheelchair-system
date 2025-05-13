package com.example.pls.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pls.R
import com.example.pls.databinding.FragmentNotificationsBinding
import com.example.pls.databinding.NotificationItemBinding
import com.example.pls.model.NotificationItem
import com.example.pls.utils.UserSessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        userSessionManager = UserSessionManager(requireContext())

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // ตรวจสอบว่าล็อกอินแล้วหรือไม่
        if (!userSessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_navigation_notifications_to_loginFragment)
            return root
        }

        setupUI()
        observeViewModel()

        return root
    }

    private fun setupUI() {
        // เซ็ต RecyclerView
        binding.notificationsList.layoutManager = LinearLayoutManager(requireContext())

        // เซ็ตปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // เซ็ตปุ่มล้างทั้งหมด
        binding.btnClearAll.setOnClickListener {
            if (_binding == null) return@setOnClickListener

            // ถามยืนยันก่อนลบ
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("ล้างการแจ้งเตือนทั้งหมด")
                .setMessage("คุณต้องการลบการแจ้งเตือนทั้งหมดหรือไม่?")
                .setPositiveButton("ล้างทั้งหมด") { _, _ ->
                    viewModel.clearAllNotifications()
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }

        // เซ็ตการดึงข้อมูลใหม่เมื่อดึงลง (swipe to refresh)
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }

    private fun observeViewModel() {
        // สังเกตการเปลี่ยนแปลงข้อมูลและอัปเดต adapter
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            // ถ้ามีข้อมูล ให้แสดง RecyclerView
            if (notifications.isNotEmpty()) {
                binding.notificationsList.visibility = View.VISIBLE
                binding.emptyNotificationsLayout.visibility = View.GONE
                binding.notificationsList.adapter = NotificationsAdapter(notifications)
            } else {
                // ถ้าไม่มีข้อมูล ให้แสดงข้อความว่าไม่มีการแจ้งเตือน
                binding.notificationsList.visibility = View.GONE
                binding.emptyNotificationsLayout.visibility = View.VISIBLE
            }
        }

        // สังเกตสถานะการโหลด
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // สังเกตข้อผิดพลาด
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter สำหรับ RecyclerView
    private inner class NotificationsAdapter(
        private val notifications: List<NotificationItem>
    ) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = NotificationItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notification = notifications[position]
            holder.bind(notification)
        }

        override fun getItemCount() = notifications.size

        inner class ViewHolder(private val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(notification: NotificationItem) {
                // ตั้งค่าภาพไอคอนตามประเภทการแจ้งเตือน
                binding.notificationIcon.setImageResource(notification.getNotificationIcon())

                // ตั้งค่าข้อความ
                binding.notificationTitle.text = notification.title
                binding.notificationContent.text = notification.content

                // ตั้งค่าเวลาและวันที่
                binding.notificationTime.text = notification.getFormattedTime()
                binding.notificationDate.text = notification.getFormattedDate()

                // ตั้งค่าแท็ก (ถ้ามี)
                if (notification.shouldShowTag() && notification.tag != null) {
                    binding.notificationTag.visibility = View.VISIBLE
                    binding.notificationTag.text = notification.tag
                } else {
                    binding.notificationTag.visibility = View.GONE
                }

                // ตั้งค่าสีพื้นหลังตามสถานะการอ่าน
                if (notification.isRead) {
                    binding.root.alpha = 0.7f
                } else {
                    binding.root.alpha = 1.0f
                }

                // ตั้งค่าการกดเพื่ออ่านการแจ้งเตือน
                binding.root.setOnClickListener {
                    // ทำเครื่องหมายว่าอ่านแล้ว
                    if (!notification.isRead) {
                        viewModel.markAsRead(notification.id)
                    }

                    // ตรวจสอบว่าเป็นการแจ้งเตือนที่นำไปยังหน้าอื่นหรือไม่
                    handleNotificationNavigation(notification)
                }

                // ตั้งค่าการกดค้างเพื่อแสดงตัวเลือกเพิ่มเติม
                binding.root.setOnLongClickListener {
                    showNotificationOptions(notification)
                    true
                }
            }

            // จัดการการนำทางเมื่อกดการแจ้งเตือน
            private fun handleNotificationNavigation(notification: NotificationItem) {
                // ตัวอย่างการนำทางตามประเภทการแจ้งเตือน
                when (notification.type) {
                    "wheelchair-status" -> {
                        // ไปที่หน้าสถานะรถวีลแชร์
                        if (notification.relatedEntityId != null) {
                            val args = Bundle().apply {
                                putString("wheelchairId", notification.relatedEntityId)
                            }
                            findNavController().navigate(R.id.navigation_dashboard, args)
                        }
                    }
                    "pairing-added", "pairing-deleted" -> {
                        // ไปที่หน้าจับคู่รถวีลแชร์
                        findNavController().navigate(R.id.pairingFragment)
                    }
                    else -> {
                        // ไม่ต้องนำทางไปไหน แค่แสดงข้อความว่าได้อ่านแล้ว
                        Toast.makeText(context, "อ่านการแจ้งเตือนแล้ว", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // แสดงตัวเลือกเพิ่มเติมเมื่อกดค้างที่การแจ้งเตือน
            private fun showNotificationOptions(notification: NotificationItem) {
                val options = arrayOf("ลบการแจ้งเตือนนี้", "ทำเครื่องหมายว่า${if (notification.isRead) "ยังไม่ได้อ่าน" else "อ่านแล้ว"}")

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("ตัวเลือก")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                // ลบการแจ้งเตือน
                                viewModel.deleteNotification(notification.id)
                            }
                            1 -> {
                                // สลับสถานะการอ่าน
                                if (notification.isRead) {
                                    // ควรมีการเพิ่ม API สำหรับทำเครื่องหมายว่ายังไม่ได้อ่าน
                                    Toast.makeText(context, "ยังไม่รองรับฟังก์ชันนี้", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        }
                    }
                    .show()
            }
        }
    }
}