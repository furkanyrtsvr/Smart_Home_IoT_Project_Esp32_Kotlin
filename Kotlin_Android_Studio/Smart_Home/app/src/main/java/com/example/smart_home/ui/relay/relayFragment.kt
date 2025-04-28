package com.example.smart_home.ui.relay

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.smart_home.MainActivity
import com.example.smart_home.R
import com.example.smart_home.databinding.FragmentRelayBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class relayFragment : Fragment() {

    private var _binding: FragmentRelayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRelayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mainActivity = activity as MainActivity


        val database = FirebaseDatabase.getInstance().reference

        // Firebase'den relay_manuel_or_date verisini oku
        val relayManuelSwitch = binding.relayManuel // Switch'i burada alıyoruz

        // relay_manuel_or_date verisini Firebase'den alıyoruz
        database.child("relay_manuel_or_date").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Firebase'den gelen değeri kontrol et
                val isManual = snapshot.getValue(Int::class.java) == 1
                relayManuelSwitch.isChecked = isManual // Switch durumunu Firebase verisine göre ayarla
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "relay_manuel_or_date verisi okunamadı: ${error.message}")
            }
        })

        // Switch değişince Firebase'e yaz
        relayManuelSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Firebase'e yeni durumu yaz
            database.child("relay_manuel_or_date").setValue(if (isChecked) 1 else 0)
        }




        // Relay durumunu izle
        mainActivity.relayStatusLiveData.observe(viewLifecycleOwner) { relayValue ->
            when (relayValue) {
                1 -> {
                    binding.relayOnPhoto.visibility = View.GONE
                    binding.relayOffPhoto.visibility = View.VISIBLE
                    binding.relayAcik.visibility = View.VISIBLE
                    binding.relayKapali.visibility = View.GONE
                }
                0 -> {
                    binding.relayOffPhoto.visibility = View.GONE
                    binding.relayOnPhoto.visibility = View.VISIBLE
                    binding.relayKapali.visibility = View.VISIBLE
                    binding.relayAcik.visibility = View.GONE
                }
            }
        }

        // Click listener'lar
        binding.relayOnPhoto.setOnClickListener {
            binding.relayOnPhoto.visibility = View.GONE
            binding.relayOffPhoto.visibility = View.VISIBLE
            binding.relayAcik.visibility = View.GONE
            binding.relayKapali.visibility = View.VISIBLE
            mainActivity.updateRelayStatus(1)
        }

        binding.relayOffPhoto.setOnClickListener {
            binding.relayOffPhoto.visibility = View.GONE
            binding.relayOnPhoto.visibility = View.VISIBLE
            binding.relayKapali.visibility = View.GONE
            binding.relayAcik.visibility = View.VISIBLE
            mainActivity.updateRelayStatus(0)
        }

        binding.viewDetails1.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_relay_to_view_details_1)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}