package com.example.pls.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pls.databinding.FragmentNotificationsBinding
import com.example.pls.databinding.NotificationItemBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // เซ็ต RecyclerView
        setupRecyclerView(notificationsViewModel)

        // เซ็ตปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return root
    }

    private fun setupRecyclerView(viewModel: NotificationsViewModel) {
        binding.notificationsList.layoutManager = LinearLayoutManager(requireContext())

        // สังเกตการเปลี่ยนแปลงข้อมูลและอัปเดต adapter
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            // ถ้ามีข้อมูล ให้แสดง RecyclerView
            if (notifications.isNotEmpty()) {
                binding.notificationsList.visibility = View.VISIBLE
                binding.textNotifications.visibility = View.GONE
                binding.notificationsList.adapter = NotificationsAdapter(notifications)
            } else {
                // ถ้าไม่มีข้อมูล ให้แสดงข้อความว่าไม่มีการแจ้งเตือน
                binding.notificationsList.visibility = View.GONE
                binding.textNotifications.visibility = View.VISIBLE
                binding.textNotifications.text = "ไม่มีการแจ้งเตือน"
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
                binding.notificationTitle.text = notification.title
                binding.notificationContent.text = notification.content
                binding.notificationDate.text = notification.date
            }
        }
    }
}