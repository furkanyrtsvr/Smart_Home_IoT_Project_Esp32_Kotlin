package com.example.smart_home.ui.light

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
import com.example.smart_home.databinding.FragmentLightBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class lightFragment : Fragment() {

    private var _binding: FragmentLightBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentLightBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // MainActivity'den LiveData'ya erişim
        val mainActivity = activity as MainActivity

        // LED durumunu izle
        mainActivity.ledStatusLiveData.observe(viewLifecycleOwner) { ledValue ->
            when (ledValue) {
                1 -> {
                    binding.ledOnPhoto.visibility = View.VISIBLE
                    binding.ledOffPhoto.visibility = View.GONE
                }
                0 -> {
                    binding.ledOnPhoto.visibility = View.GONE
                    binding.ledOffPhoto.visibility = View.VISIBLE
                }
            }
        }

        // Click listener'lar
        binding.ledOffPhoto.setOnClickListener {
            binding.ledOffPhoto.visibility = View.GONE
            binding.ledOnPhoto.visibility = View.VISIBLE
            mainActivity.updateLedStatus(1)
        }

        binding.ledOnPhoto.setOnClickListener {
            binding.ledOnPhoto.visibility = View.GONE
            binding.ledOffPhoto.visibility = View.VISIBLE
            mainActivity.updateLedStatus(0)
        }

        binding.colorPickerButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_light_to_nav_color_picker)
        }
        return root
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}